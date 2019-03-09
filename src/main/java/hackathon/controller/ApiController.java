package hackathon.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.JsonObject;
import hackathon.pojo.LoginDetails;
import hackathon.pojo.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.mvel2.templates.TemplateRuntime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.object.SqlCall;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

@RestController
@Slf4j
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ApiController {

    @RequestMapping(value = {"/v1/gameUp/signUp"}, method = RequestMethod.POST)
    public ResponseEntity<String> signUp(@RequestBody String userInfoJson) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            UserInfo userInfo = mapper.readValue(userInfoJson, UserInfo.class);
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

    @RequestMapping(value = {"/v1/gameUp/login"}, method = RequestMethod.POST)
    public ResponseEntity<String> login(@RequestBody String loginInfoJson) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            LoginDetails loginDetails = mapper.readValue(loginInfoJson, LoginDetails.class);
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
                        JsonObject apiCallReply = getLoginResponse(userInfo);
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

    public static boolean signUpDbInsert(HashMap contextMap) throws Exception {


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

    public HashMap<String, String> getContextMap(UserInfo userInfo) {
        HashMap<String, String> contextMap = new HashMap<>();
        contextMap.put("NAME", userInfo.getName());
        contextMap.put("email", userInfo.getEmail());
        contextMap.put("PASSWORD", userInfo.getPassword());
        contextMap.put("dob", userInfo.getDob());
        contextMap.put("age_grp", userInfo.getAge_grp());
        contextMap.put("gender", userInfo.getGender());
        contextMap.put("phone", userInfo.getPhone());
        contextMap.put("indoor", String.join(",", userInfo.getIndoor()));
        contextMap.put("outdoor", String.join(",", userInfo.getOutdoor()));
        return contextMap;
    }

    public static boolean userExistAlreadyInDB(HashMap contextMap, Statement statement) throws IOException, SQLException {
        String rawQuery = Resources.toString(Resources.getResource("verify.sql"), Charsets.UTF_8);
        String parsedQuery = (String) TemplateRuntime.eval(rawQuery, contextMap);
        ResultSet resultSet = statement.executeQuery(parsedQuery);
        return resultSet.next();
    }

    public static Connection getConnection() throws SQLException {
        String url = "jdbc:sqlserver://codiecon.database.windows.net:1433;database=codeicon;" +
                "user=satish@codiecon;" +
                "password=Nitssats123;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;" +
                "loginTimeout=30;";
        Connection connection = null;
        return DriverManager.getConnection(url);
    }

    private static UserInfo getUserInfo(ResultSet resultSet) throws SQLException {
        UserInfo userInfo = new UserInfo();
        userInfo.setAge_grp(resultSet.getString("age_grp"));
        userInfo.setDob(resultSet.getString("dob"));
        userInfo.setEmail(resultSet.getString("email"));
        userInfo.setGender(resultSet.getString("gender"));
        userInfo.setIndoor(new ArrayList(Arrays.asList(resultSet.getString("indoor").split(","))));
        userInfo.setOutdoor(new ArrayList(Arrays.asList(resultSet.getString("outdoor").split(","))));
        userInfo.setName(resultSet.getString("name"));
        userInfo.setPhone(resultSet.getString("phone"));
        return userInfo;
    }

    private static JsonObject getLoginResponse(UserInfo userInfo) {
        return new JsonObject();
    }
}
