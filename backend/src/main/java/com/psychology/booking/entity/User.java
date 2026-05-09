package com.psychology.booking.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    @JsonIgnore // 不返回密码给前端
    private String password;
    private String nickname;
    private String realName;
    private String phone;
    private String email;
    private String avatar;
    private Integer role; // 0-user, 1-counselor, 2-admin
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}
