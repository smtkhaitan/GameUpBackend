package hackathon.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.io.Resources;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hackathon.pojo.UserPreference;
import lombok.extern.slf4j.Slf4j;
import org.mvel2.templates.TemplateRuntime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @RequestMapping(value = {"/livhack/saveUserPreferences",method = RequestMethod.POST})
    public ResponseEntity<string> saveUserPreferences(@RequestBody String userPreferencesJson){
        try{
            ObjectMapper mapper = new ObjectMapper();
            UserPreference UserPreference = mapper.readValue(@RequestBody String userPreferencesJson)
            if (insertUserPref(getContextMap(UserPreference))) {
                return new ResponseEntity<>("SignUpSuccessfull", HttpStatus.ACCEPTED);
            } else {
                return new ResponseEntity<>("UserAlready Exist", HttpStatus.NOT_ACCEPTABLE);
            }
        }catch(Exception e){
            log.error(e.getMessage());
            return new ResponseEntity<>("Failed Try Again",HttpStatus.BAD_REQUEST);
        }
    }

    private HashMap<String, String> getContextMap(UserInfo userInfo) {
        HashMap<String, String> contextMap = new HashMap<>();
        contextMap.put("clusterTags", userInfo.clusterTags());
        contextMap.put("answers", userInfo.answers());
        return contextMap;
    }

     private static boolean insertUserPref(HashMap contextMap) throws Exception {
        try {
            Connection connection = getConnection();
            System.out.println("Query data example:");
            System.out.println("=========================================");

            String rawQuery = Resources.toString(Resources.getResource("saveUserPref.sql"), Charsets.UTF_8);
            String parsedQuery = (String) TemplateRuntime.eval(rawQuery, contextMap);
            System.out.println(parsedQuery);
            try (Statement statement = connection.createStatement()) {
                statement.execute(parsedQuery);
                connection.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
        return true;
    }

    private static Connection getConnection() throws SQLException {
        String url = "jdbc:sqlserver://codiecon.database.windows.net:1433;database=codeicon;" +
                "user=satish@codiecon;" +
                "password=Nitssats123;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;" +
                "loginTimeout=30;";
        Connection connection = null;
        return DriverManager.getConnection(url);
    }
}
