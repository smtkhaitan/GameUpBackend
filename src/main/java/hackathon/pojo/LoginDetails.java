package hackathon.pojo;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class LoginDetails {
    public String email;
    public String password;
    public String latitude;
    public String longitude;
}
