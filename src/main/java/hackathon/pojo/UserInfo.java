package hackathon.pojo;

import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;

@Data
@ToString
public class UserInfo {
    public String name;
    public String email;
    public String age;
    public String phone;
    public String password;
    public String gender;
    public String bloodGroup;
    public String weight;
    public String address;
}
