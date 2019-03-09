package hackathon.pojo;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class PairUpRegistration {
    public String user1_email;
    public String user2_email;
    public String game_type;
    public String game_time;
}
