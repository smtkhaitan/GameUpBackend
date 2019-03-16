package hackathon.pojo;

import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;

@Data
@ToString
public class UserPreference {
    public String cluster_tag;
    public String answers;
    public String user_id;
}
