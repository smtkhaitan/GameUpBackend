package hackathon.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import hackathon.pojo.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.mvel2.templates.TemplateRuntime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import java.util.HashMap;

@RestController
@Slf4j
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

    public static boolean signUpDbInsert(HashMap contextMap) throws Exception {

        // Connect to database
        String url = "jdbc:sqlserver://codiecon.database.windows.net:1433;database=codeicon;user=satish@codiecon;" +
                "password=Nitssats123;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;" +
                "loginTimeout=30;";
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(url);
            String schema = connection.getSchema();
            System.out.println("Successful connection - Schema: " + schema);

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
}
