package com.train.cloudDisk.interceptor;

import com.auth0.jwt.exceptions.AlgorithmMismatchException;
import com.auth0.jwt.exceptions.InvalidClaimException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.train.cloudDisk.model.Response;
import com.train.cloudDisk.tool.JWTUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class JWTInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        //获取请求头中的令牌
        String token = request.getHeader("Authorization");
        String msg = "";
        try {
            JWTUtils.verify(token);
            request.setAttribute("id", JWTUtils.getIdFromToken(token));
            return true;
        }
        catch (NullPointerException e) {
            msg = "令牌为空";
        } catch (SignatureVerificationException e) {
            msg = "签名验证失败";
        } catch (TokenExpiredException e) {
            msg = "令牌过期";
        } catch (AlgorithmMismatchException e) {
            msg = "算法不匹配";
        } catch (InvalidClaimException e) {
            msg = "无效的声明";
        } catch (Exception e) {
            msg = "令牌解析失败";
        }
        String code = "401";
        //响应到前台: 将map转为json
        Response r = new Response(code, msg, null);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().println(r.toJSON());
        return false;
    }
}