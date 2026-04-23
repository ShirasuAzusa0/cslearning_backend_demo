package ben.qihuiadmin.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class AuthorVO {
    private long userId;
    private Attributes attributes;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Attributes {
        private String avatarUrl;
        private String userName;
        private String email;
    }
}
