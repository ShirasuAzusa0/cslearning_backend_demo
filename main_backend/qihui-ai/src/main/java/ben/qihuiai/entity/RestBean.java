package ben.qihuiai.entity;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;

// 统一后端API响应格式的封装对象，封装响应的基本信息，用于规范化返回json数据
// 返回信息包括状态（status）、说明信息（msg）、数据（data），接下来封装几个工具方法
public record RestBean<T>(String status, String msg, T data) {
    // 请求工具
    public static <T> RestBean<T> successKey(T key) {
        return new RestBean<>("success", "获取公钥成功", key);
    }

    public static <T> RestBean<T> successType1(String msg, T data) {
        return new RestBean<>("success", msg, data);
    }

    public static <T> RestBean<T> successType2(String msg) {
        return new RestBean<>("success", msg, null);
    }

    public static <T> RestBean<T> successType3(String msg, T sessionId) {
        return new RestBean<>("success", msg, sessionId);
    }

    public static <T> RestBean<T> successType4(String msg, T messageId) {
        return new RestBean<>("success", msg, messageId);
    }

    public static <T> RestBean<T> successType5(String msg, T kbId) {
        return new RestBean<>("success", msg, kbId);
    }

    public static <T> RestBean<T> successType6(String msg, T documentIds) {
        return new RestBean<>("success", msg, documentIds);
    }

    public static <T> RestBean<T> successType7(String msg, T judegeResult) {
        return new RestBean<>("success", msg, judegeResult);
    }

    // 请求失败工具方法
    public static <T> RestBean<T> failure(String msg) {
        return new RestBean<>("fail", msg, null);
    }

    // 未登录无权限工具方法
    public static <T> RestBean<T> unauthorized(String msg) {
        return new RestBean<>("unauthorized", msg, null);
    }

    public static <T> RestBean<T> forbidden(String msg) {
        return new RestBean<>("forbidden", msg, null);
    }

    // 转换为json格式的工具方法
    public String asJsonString() {
        // WriteNulls可以处理空值
        return JSONObject.toJSONString(this, JSONWriter.Feature.WriteNulls);
    }
}
