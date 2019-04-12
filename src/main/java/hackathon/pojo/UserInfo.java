package hackathon.pojo;

import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;

@Data
@ToString
public class UserInfo {
    public String name;
    public String email;
    public String dob;
    public String age_grp;
    public String phone;
    public String password;
    public String gender;
    public ArrayList<String> outdoor;
    public ArrayList<String> indoor;
}
