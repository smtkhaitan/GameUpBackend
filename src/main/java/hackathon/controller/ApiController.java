package hackathon.controller;

import com.google.gson.JsonObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiController {

    @RequestMapping(value = {"/v1/testing/get"}, method = RequestMethod.GET)
    public ResponseEntity<String> getRandomJson(
            @RequestParam(value = "info", required = false) String eventInfoProt
    ) {
        JsonObject test = new JsonObject();
        test.addProperty("name","sumit");
        return new ResponseEntity<>(eventInfoProt,HttpStatus.ACCEPTED);
    }

    @RequestMapping(value = {"/v1"}, method = RequestMethod.GET)
    public ResponseEntity<String> getRandomJsonT() {
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }
}
