import java.io.*;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class PongSession implements Runnable {

    private final PingPongProtocol mode;
    private final BufferedWriter out;
    private final BufferedReader in;
    private String map;
    private final File mapFile;
    private final Ships ships;
    private int invalidMessage;
    private String opponentsMap;
    private Instant start;
    private Instant end;

    private static final String START = "start";
    private static final String MISS = "miss";
    private static final String LAST_SUNK = "last sunk";
    private static final String HIT = "hit";
    private static final String HIT_AND_SUNK = "hit and sunk";
    private static final String COMMUNICATION_ERROR = "Communication error";

    public PongSession(Socket socket, PingPongProtocol mode, File mapFile, Ships ships) throws IOException {
        this.mode = mode;
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.mapFile = mapFile;
        this.ships = ships;
    }

    private void extractMap() throws FileNotFoundException {
        Scanner scanner = new Scanner(mapFile);
        this.map = scanner.nextLine();
        scanner.close();
    }

    private String extractCoordinates(String message) {
        String separator = ";";
        int separatorPosition = message.indexOf(separator);
        return message.substring(separatorPosition + 1);
    }

    private String extractCommand(String message) {
        if(message.equals(LAST_SUNK)) {
            return message.trim();
        }
        String separator = ";";
        int separatorPosition = message.indexOf(separator);
        return message.substring(0, separatorPosition);
    }

    public boolean validMessage(String message) {
        String command = extractCommand(message);
        return command.equals(START) || command.equals(MISS) || command.equals(LAST_SUNK) || command.equals(HIT) || command.equals(HIT_AND_SUNK);
    }

    public void exitGame() {
        System.out.println(COMMUNICATION_ERROR);
        System.exit(0);
    }

    private void revealAll(){
        opponentsMap = opponentsMap.replace("?", ".");
    }

    private void manageOpponentsAnswer(String message, String previousMessage) {
        if(!extractCommand(message).equals(START) && !previousMessage.equals("")) {
            manageOpponentsMap(extractCoordinates(previousMessage), extractCommand(message));
        }
    }

    private void manageOpponentsLoss(String message, String previousMessage) {
        manageOpponentsMap(extractCoordinates(previousMessage), extractCommand(message));
        revealAll();
    }

    private boolean communicationProblem(String message) {
        return !validMessage(message) ||
                (!extractCommand(message).equals(START) && Duration.between(start, end).compareTo(Duration.ofSeconds(1)) > 0);
    }

    private void manageComunicationProblem(String previousMessage) {
        invalidMessage++;
        if(invalidMessage == 3) {
            exitGame();
        }
        try {
            send(previousMessage);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public void prepareMaps() throws FileNotFoundException {
        extractMap();
        fulfillOpponentsMap();
    }

    public void manageStart() throws IOException {
        if (mode == PingPongProtocol.PING) {
            send("start;" + randomizeCoordinates() + "\n");
        }
    }

    public void gotMessage(String message) {
        System.out.println("[" + Thread.currentThread().getName() + "] got " + message + "!");
    }

    public void run() {
        try {
            prepareMaps();
            manageStart();
            String previousMessage = "";
            String message = "";

            while (true) {
                if(!in.ready()) {
                    continue;
                }

                message = in.readLine();
                end = Instant.now();

                if(message.isEmpty()) {
                    continue;
                }

                message = message.strip();

                if(communicationProblem(message)) {
                    manageComunicationProblem(previousMessage);
                } else {
                    invalidMessage = 0;
                    gotMessage(message);

                    if(opponentLost(message)) {
                        manageOpponentsLoss(message, previousMessage);
                        win();
                    }

                    manageOpponentsAnswer(message, previousMessage);

                    String commandToSednd = manageMoves(message);
                    previousMessage = manageSending(commandToSednd).strip();

                    if(iLost(commandToSednd)) {
                        loss();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void manageOpponentsMap(String coordinates, String command) {
        markHitOnMap(coordinates, command, MapsOwner.OPPONENT);
    }

    private boolean opponentLost(String message) {
        String command = extractCommand(message);
        return LAST_SUNK.equals(command);
    }

    private boolean iLost(String command) {
        return LAST_SUNK.equals(command);
    }

    private void win() {
        System.out.print("You won :)");
        System.out.println("Opponent's map:");
        displayMap(MapsOwner.OPPONENT);
        System.out.println();
        System.out.println("Your map:");
        displayMap(MapsOwner.ME);
        System.exit(0);
    }

    private void loss() {
        System.out.println("You lost :(");
        System.out.println("Opponent's map:");
        displayMap(MapsOwner.OPPONENT);
        System.out.println();
        System.out.println("Your map:");
        displayMap(MapsOwner.ME);
        System.exit(0);
    }

    private String manageSending(String command) throws IOException {
        String message = command + ";" + randomizeCoordinates() + "\n";
        send(message);
        return message;
    }

    private void sendMessage(String message) {
        System.out.println("[" + Thread.currentThread().getName() + "] sending " + message);
    }

    private void send(String toSend) throws IOException {
        start = Instant.now();
        sendMessage(toSend);
        out.write(toSend);
        out.newLine();
        out.flush();
    }

    private String randomizeCoordinates(){
        Random randomNum = new Random(); 
        int n = randomNum.nextInt(10) + 1; 
        Random randomLetter = new Random(); 
        int c = randomLetter.nextInt(10) + 65; 
        char letter = (char)c;
        return letter + Integer.toString(n);
    }

    private static int extractFirstCoordinate(String coordinates) {
        return (int)coordinates.charAt(0) - 65;
    }

    private static int extractSecondCoordinate(String coordinates) {
        coordinates = coordinates.substring(1);
        return Integer.parseInt(coordinates) - 1;
    }

    private int getRealCoordinateInMap(int coordinate1, int coordinate2) {
        return coordinate1 * 10 + coordinate2;
    }

    public void replaceChar(MapsOwner mapsOwner, char ch, int index) {
        if(MapsOwner.ME.equals(mapsOwner)) {
            map = map.substring(0, index) + ch + map.substring(index + 1);
        } else {
            opponentsMap = opponentsMap.substring(0, index) + ch + opponentsMap.substring(index + 1);
        }
    }


    private void markField(String coordinates, Character sign, MapsOwner mapsOwner) {
        int c1 = extractFirstCoordinate(coordinates);
        int c2 = extractSecondCoordinate(coordinates);
        int coordinateInMap = getRealCoordinateInMap(c1, c2);
        replaceChar(mapsOwner, sign, coordinateInMap);
    }

    private void miss(String coordinates, MapsOwner mapsOwner) {
        if(mapsOwner.equals(MapsOwner.ME)) {
            markField(coordinates, '~', mapsOwner);
        } else {
            markField(coordinates, '.', mapsOwner);
        }
    }

    private void hit(String coordinates, String resultCommand, MapsOwner mapsOwner) {
        if(mapsOwner.equals(MapsOwner.ME)) {
            markField(coordinates, '@', mapsOwner);
        } else {
            markField(coordinates, '#', mapsOwner);
            if(HIT_AND_SUNK.equals(resultCommand) || LAST_SUNK.equals(resultCommand)) {
                markAdjacentFields(coordinates);
            }
        }
    }

    private void markHitOnMap(String coordinates, String resultCommand, MapsOwner mapsOwner) {
        if (MISS.equals(resultCommand)) {
            miss(coordinates, mapsOwner);
        } else {
            hit(coordinates, resultCommand, mapsOwner);
        }
    }

    private String manageMoves(String message) {
        String coordinates = extractCoordinates(message);
        String resultCommand = ships.manageHit(coordinates);
        markHitOnMap(coordinates, resultCommand, MapsOwner.ME);
        return resultCommand;
    }

    private void displayMap(MapsOwner mapsOwner) {
        for(int i = 0; i < 100; i++) {
            if(i != 0 && i % 10 == 0) {
                System.out.print("\n");
            }
            if(mapsOwner.equals(MapsOwner.ME)) {
                System.out.print(map.charAt(i));
            } else {
                System.out.print(opponentsMap.charAt(i));
            }
        }
        System.out.print("\n");
    }

    private void fulfillOpponentsMap() {
        opponentsMap = "?".repeat(100);
    }

    private void findAllMastsOfShip(int coordinates, List<Integer> allMasts) {
        if(opponentsMap.charAt(coordinates) == '#') {
            if (allMasts.contains(coordinates)) {
                return;
            }
            allMasts.add(coordinates);

            if (coordinates >= 10) {
                findAllMastsOfShip(coordinates - 10, allMasts);
            }
            if (coordinates % 10 != 0) {
                findAllMastsOfShip(coordinates - 1, allMasts);
            }
            if (coordinates % 10 != 9) {
                findAllMastsOfShip(coordinates + 1, allMasts);
            }
            if (coordinates < 90) {
                findAllMastsOfShip(coordinates + 10, allMasts);
            }
        }
    }

    private void findAllAdjacentToFiled(int coordinates, List<Integer> allAdjacentFields) {
        if(coordinates >= 10) {
            if(opponentsMap.charAt(coordinates - 10) == '?') {
                allAdjacentFields.add(coordinates - 10);
            }
            if(coordinates % 10 != 0 && opponentsMap.charAt(coordinates - 11) == '?') {
                allAdjacentFields.add(coordinates - 11);
            }
            if(coordinates % 10 != 9 && opponentsMap.charAt(coordinates - 9) == '?') {
                allAdjacentFields.add(coordinates - 9);
            }
        }
        if(coordinates % 10 != 0 && opponentsMap.charAt(coordinates - 1) == '?') {
            allAdjacentFields.add(coordinates - 1);
        }
        if(coordinates % 10 != 9 && opponentsMap.charAt(coordinates + 1) == '?') {
            allAdjacentFields.add(coordinates + 1);
        }
        if(coordinates < 90) {
            if(opponentsMap.charAt(coordinates + 10) == '?') {
                allAdjacentFields.add(coordinates + 10);
            }
            if(coordinates % 10 != 0 && opponentsMap.charAt(coordinates + 9) == '?') {
                allAdjacentFields.add(coordinates + 9);
            }
            if(coordinates % 10 != 9 && opponentsMap.charAt(coordinates + 11) == '?') {
                allAdjacentFields.add(coordinates + 11);
            }
        }
    }

    private void findAllAdjacentFiledsTo(List<Integer> allMastsOfShip, List<Integer> allAdjacentFields) {
        for(Integer coordinates : allMastsOfShip) {
            findAllAdjacentToFiled(coordinates, allAdjacentFields);
        }
    }

    private List<Integer> removeRepetition(List<Integer> list) {
        Set<Integer> temp = new HashSet<>(list);
        return new ArrayList<>(temp);
    }

    private void markAllAdjacentFields(List<Integer> fields) {
        for(Integer field : fields) {
            replaceChar(MapsOwner.OPPONENT,'.',field);
        }
    }

    private void markAdjacentFields(String coordinates) {
        int coordinatesInMap = getRealCoordinateInMap(extractFirstCoordinate(coordinates), extractSecondCoordinate(coordinates));
        List<Integer> allMastsOfShip = new ArrayList<>();
        findAllMastsOfShip(coordinatesInMap, allMastsOfShip);
        List<Integer> allAdjacentFieldsWithRepetition = new ArrayList<>();
        findAllAdjacentFiledsTo(allMastsOfShip, allAdjacentFieldsWithRepetition);
        List<Integer> allAdjacentFields = removeRepetition(allAdjacentFieldsWithRepetition);
        markAllAdjacentFields(allAdjacentFields);
    }

}