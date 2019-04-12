package hackathon.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hackathon.pojo.BookingDetails;
import hackathon.pojo.LoginDetails;
import hackathon.pojo.PairUp;
import hackathon.pojo.PairUpRegistration;
import hackathon.pojo.SelfRegistration;
import hackathon.pojo.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.mvel2.templates.TemplateRuntime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@RestController
@Slf4j
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ApiController {

    @RequestMapping(value = {"/v1/gameUp/signUp"}, method = RequestMethod.POST)
    public ResponseEntity<String> signUp(@RequestBody String userInfoJson) {
        System.out.println("In SignUp");
        try {
            ObjectMapper mapper = new ObjectMapper();
            UserInfo userInfo = mapper.readValue(userInfoJson, UserInfo.class);
            System.out.println("SignUp : " +userInfo);
            if (signUpDbInsert(getContextMap(userInfo))) {
                return new ResponseEntity<>("SignUpSuccessfull", HttpStatus.ACCEPTED);
            } else {
                return new ResponseEntity<>("UserAlready Exist", HttpStatus.NOT_ACCEPTABLE);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>("Failed Try Again", HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = {"/"})
    public String testMethod(@RequestParam String id) {
        return "id received: " + id;
    }

    @RequestMapping(value = {"/v1/gameUp/login"}, method = RequestMethod.POST)
    public ResponseEntity<String> login(@RequestBody String loginInfoJson) {
        System.out.println("In login");
        try {
            ObjectMapper mapper = new ObjectMapper();
            LoginDetails loginDetails = mapper.readValue(loginInfoJson, LoginDetails.class);

            System.out.println("Login : "+loginDetails);
            if (StringUtils.isEmpty(loginDetails.getEmail()) || StringUtils.isEmpty(loginDetails.getPassword())) {
                return new ResponseEntity<>("Email or Password Can't be Empty", HttpStatus.BAD_REQUEST);
            }

            //@todo:kalyani to make sure without lat long login is not called

            Connection connection = getConnection();

            //verify if user is present in the db.
            String rawQuery = Resources.toString(Resources.getResource("verify.sql"), Charsets.UTF_8);
            HashMap<String, String> contextMap = new HashMap<>();
            contextMap.put("email", loginDetails.getEmail());
            String parsedQuery = (String) TemplateRuntime.eval(rawQuery, contextMap);

            try (Statement statement = connection.createStatement()) {
                ResultSet resultSet = statement.executeQuery(parsedQuery);
                while (resultSet.next()) {
                    String dbPassword = resultSet.getString("password");
                    if (!dbPassword.equals(loginDetails.getPassword())) {
                        connection.close();
                        return new ResponseEntity<>("Wrong Password", HttpStatus.UNAUTHORIZED);
                    } else {
                        UserInfo userInfo = getUserInfo(resultSet);
                        System.out.println("UserInfo: " + userInfo);
                        JsonObject apiCallReply = getLoginResponse(userInfo, loginDetails);
                        connection.close();
                        return new ResponseEntity<>(apiCallReply.toString(), HttpStatus.ACCEPTED);
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>("Failed Try Again", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>("Try Again", HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value = {"/v1/gameUp/selfRegistration"}, method = RequestMethod.POST)
    public ResponseEntity<String> selfRegistration(@RequestBody String selfRegistrationJson) {
        System.out.println("In selfRegistration");
        try {
            ObjectMapper mapper = new ObjectMapper();
            SelfRegistration selfRegistration = mapper.readValue(selfRegistrationJson, SelfRegistration.class);
            if (StringUtils.isEmpty(selfRegistration.getEmail()) || StringUtils.isEmpty(selfRegistration.getGame_type())
                    || StringUtils.isEmpty(selfRegistration.getGame_time())
                    || StringUtils.isEmpty(selfRegistration.getLatitude())
                    || StringUtils.isEmpty(selfRegistration.getLongitude())) {
                return new ResponseEntity<>("Information can't be empty", HttpStatus.BAD_REQUEST);
            }

            Connection connection = getConnection();

            String rawQuery = Resources.toString(Resources.getResource("selfRegistration.sql"), Charsets.UTF_8);
            HashMap<String, String> contextMap = new HashMap<>();
            contextMap.put("user_email", selfRegistration.getEmail());
            contextMap.put("game_type", selfRegistration.getGame_type());
            contextMap.put("game_time", selfRegistration.getGame_time());
            contextMap.put("paired", "0");
            contextMap.put("lat_long", selfRegistration.getLatitude() + "#" + selfRegistration.getLongitude());
            String parsedQuery = (String) TemplateRuntime.eval(rawQuery, contextMap);

            try (Statement statement = connection.createStatement()) {
                statement.execute(parsedQuery);
                connection.close();
                return new ResponseEntity<>("Successfull", HttpStatus.ACCEPTED);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>("Failed Try Again", HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = {"/v1/gameUp/pairUpRegistration"}, method = RequestMethod.POST)
    public ResponseEntity<String> pairUpRegistration(@RequestBody String pairUpRegistrationJson) {
        System.out.println("In pairUpRegistration");
        try {
            ObjectMapper mapper = new ObjectMapper();
            PairUpRegistration pairUpRegistration = mapper.readValue(pairUpRegistrationJson, PairUpRegistration.class);
            if (StringUtils.isEmpty(pairUpRegistration.getUser1_email())
                    || StringUtils.isEmpty(pairUpRegistration.getUser2_email())
                    || StringUtils.isEmpty(pairUpRegistration.getGame_time())
                    || StringUtils.isEmpty(pairUpRegistration.getGame_type())) {
                return new ResponseEntity<>("Information can't be empty", HttpStatus.BAD_REQUEST);
            }

            Connection connection = getConnection();

            String rawQuery = Resources.toString(Resources
                    .getResource("selfRegistration.sql"), Charsets.UTF_8);
            HashMap<String, String> contextMap = new HashMap<>();

            // user1 is the one who is logged in. Entry for user2 will already be there in the db, need to set its
            // paired status as 1
            contextMap.put("user_email", pairUpRegistration.getUser1_email());
            contextMap.put("game_type", pairUpRegistration.getGame_type());
            contextMap.put("game_time", pairUpRegistration.getGame_time());
            contextMap.put("paired", "1");

            //@todo:sumit.khaitan come up with a better logic .Keeping this as default for now
            contextMap.put("lat_long", "999999#999999");
            String parsedQuery = (String) TemplateRuntime.eval(rawQuery, contextMap);

            try (Statement statement = connection.createStatement()) {
                statement.execute(parsedQuery);

                // need to update paired status for user 2
                rawQuery = Resources.toString(Resources
                        .getResource("selfRegistrationUpdatePairedStatus.sql"), Charsets.UTF_8);
                contextMap.clear();
                contextMap.put("user_email", pairUpRegistration.getUser2_email());
                contextMap.put("game_type", pairUpRegistration.getGame_type());
                contextMap.put("game_time", pairUpRegistration.getGame_time());
                //@todo:sumit.khaitan come up with a better logic .Keeping this as default for now
                contextMap.put("lat_long", "-1#-1");

                parsedQuery = (String) TemplateRuntime.eval(rawQuery, contextMap);
                statement.execute(parsedQuery);


                //need to make entry in paired table as well
                rawQuery = Resources.toString(Resources.getResource("pairUpEntry.sql"), Charsets.UTF_8);
                contextMap.clear();
                contextMap.put("user1_email", pairUpRegistration.getUser1_email());
                contextMap.put("user2_email", pairUpRegistration.getUser2_email());
                contextMap.put("game_type", pairUpRegistration.getGame_type());
                contextMap.put("game_time", pairUpRegistration.getGame_time());
                parsedQuery = (String) TemplateRuntime.eval(rawQuery, contextMap);
                statement.execute(parsedQuery);
                connection.close();
                return new ResponseEntity<>("Successfull", HttpStatus.ACCEPTED);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>("Failed Try Again", HttpStatus.BAD_REQUEST);
        }
    }

    private static boolean signUpDbInsert(HashMap contextMap) throws Exception {
        try {
            Connection connection = getConnection();
            System.out.println("Query data example:");
            System.out.println("=========================================");

            String rawQuery = Resources.toString(Resources.getResource("signUp.sql"), Charsets.UTF_8);
            String parsedQuery = (String) TemplateRuntime.eval(rawQuery, contextMap);
            System.out.println(parsedQuery);
            try (Statement statement = connection.createStatement()) {
                if (!userExistAlreadyInDB(contextMap, statement)) {
                    statement.execute(parsedQuery);
                    connection.close();
                } else {
                    connection.close();
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
        return true;
    }

    private HashMap<String, String> getContextMap(UserInfo userInfo) {
        HashMap<String, String> contextMap = new HashMap<>();
        contextMap.put("name", userInfo.getName());
        contextMap.put("email", userInfo.getEmail());
        contextMap.put("password", userInfo.getPassword());
        contextMap.put("age", userInfo.getAge());
        contextMap.put("gender", userInfo.getGender());
        contextMap.put("phone", userInfo.getPhone());
        contextMap.put("blood_group", userInfo.getBloodGroup());
        contextMap.put("weight", userInfo.getWeight());
        contextMap.put("address", userInfo.getAddress());
        return contextMap;
    }

    private static boolean userExistAlreadyInDB(HashMap contextMap, Statement statement)
            throws IOException, SQLException {
        String rawQuery = Resources.toString(Resources.getResource("verify.sql"), Charsets.UTF_8);
        String parsedQuery = (String) TemplateRuntime.eval(rawQuery, contextMap);
        ResultSet resultSet = statement.executeQuery(parsedQuery);
        return resultSet.next();
    }

    private static Connection getConnection() throws SQLException {
        String url = "jdbc:sqlserver://idsp-cluster-preprod-v2-sqlserver.database.windows.net:1433;" +
                "database=warehouse;user=platform@idsp-cluster-preprod-v2-sqlserver;password=iDspData2018;encrypt=true;" +
                "trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;";
        return DriverManager.getConnection(url);
    }

    private static UserInfo getUserInfo(ResultSet resultSet) throws SQLException {
        UserInfo userInfo = new UserInfo();
        userInfo.setAge(resultSet.getString("age"));
        userInfo.setEmail(resultSet.getString("email"));
        userInfo.setGender(resultSet.getString("gender"));
        userInfo.setName(resultSet.getString("name"));
        userInfo.setPhone(resultSet.getString("phone"));
        return userInfo;
    }

    private static JsonObject getLoginResponse(UserInfo userInfo, LoginDetails loginDetails) throws Exception {
        JsonObject jsonObject = new JsonObject();
        Gson gson = new Gson();
        jsonObject.addProperty("userInfo", gson.toJson(userInfo));

        List<BookingDetails> bookingDetailsList = getBookingDetails(userInfo.getEmail());
        JsonElement element = gson.toJsonTree(bookingDetailsList, new TypeToken<List<BookingDetails>>() {
        }.getType());
        jsonObject.addProperty("bookingDetailsList", element.getAsJsonArray().toString());

        List<PairUp> pairUpList = getPairUpList(userInfo, loginDetails);
        element = gson.toJsonTree(pairUpList, new TypeToken<List<PairUp>>() {
        }.getType());
        jsonObject.addProperty("usersAvailableToPairUp", element.getAsJsonArray().toString());
        return jsonObject;
    }

    private static List<BookingDetails> getBookingDetails(String emailId) throws Exception {
        Connection connection = getConnection();
        String rawQuery = Resources.toString(Resources.getResource("checkBookedGame.sql"), Charsets.UTF_8);
        HashMap<String, String> contextMap = new HashMap<>();
        contextMap.put("email", emailId);
        String parsedQuery = (String) TemplateRuntime.eval(rawQuery, contextMap);
        List<BookingDetails> bookingDetailsList = new ArrayList<>();

        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(parsedQuery);
            while (resultSet.next()) {
                if (todaysDate(resultSet.getString("game_time"))) {
                    BookingDetails bookingDetails = new BookingDetails();
                    bookingDetails.setGame(resultSet.getString("game_type"));
                    bookingDetails.setTime(resultSet.getString("game_time"));
                    bookingDetailsList.add(bookingDetails);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
        connection.close();
        System.out.println(bookingDetailsList);
        return bookingDetailsList;
    }

    private static boolean todaysDate(String epochTime) {
        SimpleDateFormat df2 = new SimpleDateFormat("dd/MM/yy");
        Long currentTime = System.currentTimeMillis();
        Long gameTime = Long.parseLong(epochTime);
        Date gameDate = new Date(gameTime);
        Date currentDate = new Date(currentTime);
        System.out.println(gameDate);
        System.out.println(currentDate);
        return (df2.format(gameDate).equals(df2.format(currentDate)));
    }

    private static double distance(double lat1, double lat2, double lon1, double lon2) {

        // The math module contains a function
        // named toRadians which converts from
        // degrees to radians.
        lon1 = Math.toRadians(lon1);
        lon2 = Math.toRadians(lon2);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        // Haversine formula
        double dlon = lon2 - lon1;
        double dlat = lat2 - lat1;
        double a = Math.pow(Math.sin(dlat / 2), 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.pow(Math.sin(dlon / 2), 2);

        double c = 2 * Math.asin(Math.sqrt(a));

        // Radius of earth in kilometers. Use 3956
        // for miles
        double r = 6371;

        // calculate the result
        return (c * r) * 1000;
    }

    private static List<PairUp> getPairUpList(UserInfo userInfo, LoginDetails loginDetails) throws Exception {
        List<PairUp> pairUpList = new ArrayList<>();
        Connection connection = getConnection();
        String rawQuery = Resources.toString(Resources.getResource("pairUp.sql"), Charsets.UTF_8);
        HashMap<String, String> contextMap = new HashMap<>();
        contextMap.put("email", userInfo.getEmail());
        String parsedQuery = (String) TemplateRuntime.eval(rawQuery, contextMap);
        System.out.println(parsedQuery);
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(parsedQuery);
            while (resultSet.next()) {
                double loggedUserLat = Double.parseDouble(loginDetails.getLatitude());
                double loggedUserLong = Double.parseDouble(loginDetails.getLongitude());
                double pairedUserLat = Double.parseDouble(resultSet.getString("lat_long").split("#")[0]);
                double pairedUserLong = Double.parseDouble(resultSet.getString("lat_long").split("#")[1]);
                double distance = distance(loggedUserLat, pairedUserLat, loggedUserLong, pairedUserLong);
                System.out.println(distance);
                if (distance < 5000L && todaysDate(resultSet.getString("game_time"))) {
                    PairUp pairUp = new PairUp();
                    pairUp.setAgeGroup(resultSet.getString("age_grp"));
                    pairUp.setEmail(resultSet.getString("email"));
                    pairUp.setGame(resultSet.getString("game_type"));
                    pairUp.setGender(resultSet.getString("gender"));
                    pairUp.setName(resultSet.getString("name"));
                    pairUp.setTime(resultSet.getString("game_time"));
                    pairUp.setLatitude(Double.toString(pairedUserLat));
                    pairUp.setLongitude(Double.toString(pairedUserLong));
                    pairUpList.add(pairUp);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            connection.close();
            throw new Exception();
        }
        connection.close();
        return pairUpList;
    }

    @RequestMapping(value = {"/v1/gameUp/test"}, method = RequestMethod.POST)
    public ResponseEntity<String> test(@RequestBody String testJson) throws Exception {
        System.out.println("In test");

        String url = "jdbc:mysql://mysql-donateit.1d35.starter-us-east-1.openshiftapps.com/userprofile";
        Class.forName ("com.mysql.jdbc.Driver").newInstance ();
        Connection conn = DriverManager.getConnection (url, "admin", "admin");
        System.out.println("Got connection  :" + conn);

        try {
            try (Statement statement = conn.createStatement()) {
                // need to update paired status for user 2
                String rawQuery = Resources.toString(Resources
                        .getResource("test.sql"), Charsets.UTF_8);
                System.out.println(rawQuery);
                ResultSet rs = statement.executeQuery(rawQuery);
                System.out.println("ResultSet :" + rs);
                while (rs.next())
                {
                    String firstName = rs.getString("name");
                    String lastName = rs.getString("email");
                    String dateCreated = rs.getString("password");
                    String isAdmin = rs.getString("gender");
                    String numPoints = rs.getString("phone");

                    // print the results
                    System.out.format(" %s, %s, %s, %s, %s\n", firstName, lastName, dateCreated, isAdmin, numPoints);
                }
                conn.close();
                return new ResponseEntity<>("Successfull", HttpStatus.ACCEPTED);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>("Failed Try Again", HttpStatus.BAD_REQUEST);
        }
    }
}
