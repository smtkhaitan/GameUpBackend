package hackathon.pojo;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class SelfRegistration {
    public String email;
    public String game_type;
    public String game_time;
    public String latitude;
    public String longitude;
}
