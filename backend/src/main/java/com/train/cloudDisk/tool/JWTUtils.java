package com.train.cloudDisk.tool;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.train.cloudDisk.model.User;

import java.util.Date;
import java.util.List;


public class JWTUtils {
    private static String secretKey = "secretKey"; // 你的密钥

    // 生成Token的方法
    public static String generateToken(User user) {
        Date start = new Date();
        long currentTime = System.currentTimeMillis() + 604800000;//一周有效时间
        Date end = new Date(currentTime);
        String token = "";
        token = JWT.create()
                .withAudience(user.getId().toString())
                .withIssuedAt(start)
                .withExpiresAt(end)
                .sign(Algorithm.HMAC256(secretKey));
        return token;
    }

    public static String getIdFromToken(String token) {
        List<String> list= JWT.decode(token).getAudience();
        return list.get(0);
    }

    public static DecodedJWT verify(String token) {
        //如果有任何验证异常，此处都会抛出异常
        return JWT.require(Algorithm.HMAC256(secretKey)).build().verify(token);
    }

}
