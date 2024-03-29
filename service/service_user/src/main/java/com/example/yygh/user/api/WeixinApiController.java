package com.example.yygh.user.api;

import com.alibaba.fastjson.JSONObject;
import com.example.yygh.common.exception.YyghException;
import com.example.yygh.common.helper.JwtHelper;
import com.example.yygh.common.result.Result;
import com.example.yygh.common.result.ResultCodeEnum;
import com.example.yygh.model.user.UserInfo;
import com.example.yygh.user.service.UserInfoService;
import com.example.yygh.user.utils.ConstantPropertiesUtil;
import com.example.yygh.user.utils.HttpClientUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import static org.reflections.Reflections.log;

@Controller
@RequestMapping("/api/ucenter/wx")
public class WeixinApiController {

    @Autowired
    private UserInfoService userInfoService;

    /**
     * 微信登录回调
     *
     * @param code
     * @param state
     * @return
     */
    @RequestMapping("callback")
    public String callback(String code, String state) {
        // 获取授权临时票据
        if (StringUtils.isEmpty(state) || StringUtils.isEmpty(code)) {
            log.error("非法回调请求");
            throw new YyghException(ResultCodeEnum.ILLEGAL_CALLBACK_REQUEST_ERROR);
        }
        //使用code和appid以及appscrect换取access_token
        StringBuffer baseAccessTokenUrl = new StringBuffer()
                .append("https://api.weixin.qq.com/sns/oauth2/access_token")
                .append("?appid=%s")
                .append("&secret=%s")
                .append("&code=%s")
                .append("&grant_type=authorization_code");

        String accessTokenUrl = String.format(baseAccessTokenUrl.toString(),
                ConstantPropertiesUtil.WX_OPEN_APP_ID,
                ConstantPropertiesUtil.WX_OPEN_APP_SECRET,
                code);

        // 使用 httpclient 请求地址
        String result = null;
        // 使用code换取的access_token
        try {
            result = HttpClientUtils.get(accessTokenUrl);
        } catch (Exception e) {
            throw new YyghException(ResultCodeEnum.FETCH_ACCESSTOKEN_FAILD);
        }

        // 获取 result 中的数据
        JSONObject resultJson = JSONObject.parseObject(result);
        if(resultJson.getString("errcode") != null){
            log.error("获取access_token失败：" + resultJson.getString("errcode") + resultJson.getString("errmsg"));
            throw new YyghException(ResultCodeEnum.FETCH_ACCESSTOKEN_FAILD);
        }
        String accessToken = resultJson.getString("access_token");
        String openId = resultJson.getString("openid");
        log.info(accessToken);
        log.info(openId);

        //先根据openid进行数据库查询
         UserInfo userInfo = userInfoService.selectWxInfoOpenId(openId);
        // 如果没有查到用户信息,那么调用微信个人信息获取的接口
         if(null == userInfo){
             //如果查询到个人信息，那么直接进行登录
             //使用access_token换取受保护的资源：微信的个人信息
             String baseUserInfoUrl = "https://api.weixin.qq.com/sns/userinfo" +
                     "?access_token=%s" +
                     "&openid=%s";
             String userInfoUrl = String.format(baseUserInfoUrl, accessToken, openId);
             String resultUserInfo = null;
             // 使用access_token获取用户信息
             try {
                 resultUserInfo = HttpClientUtils.get(userInfoUrl);
             } catch (Exception e) {
                 throw new YyghException(ResultCodeEnum.FETCH_USERINFO_ERROR);
             }

             JSONObject resultUserInfoJson = JSONObject.parseObject(resultUserInfo);
             if(resultUserInfoJson.getString("errcode") != null){
                 log.error("获取用户信息失败：" + resultUserInfoJson.getString("errcode") + resultUserInfoJson.getString("errmsg"));
                 throw new YyghException(ResultCodeEnum.FETCH_USERINFO_ERROR);
             }

             //解析用户信息
             String nickname = resultUserInfoJson.getString("nickname");
             String headimgurl = resultUserInfoJson.getString("headimgurl");

             // 扫描注册的账号信息添加到数据库
             userInfo = new UserInfo();
             userInfo.setOpenid(openId);
             userInfo.setNickName(nickname);
             userInfo.setStatus(1);
             userInfoService.save(userInfo);
         }


        // 返回数据
        Map<String, Object> map = new HashMap<>();
        String name = userInfo.getName();
        if(StringUtils.isEmpty(name)) {
            name = userInfo.getNickName();
        }
        if(StringUtils.isEmpty(name)) {
            name = userInfo.getPhone();
        }
        map.put("name", name);
        // 判断userInfo 是否有手机号，如果手机号为空，返回 openid
        // 如果手机号不为空，返回openid为空字符串
        if(StringUtils.isEmpty(userInfo.getPhone())) {
            map.put("openid", userInfo.getOpenid());
        } else {
            map.put("openid", "");
        }
        // 生成 token
        String token = JwtHelper.createToken(userInfo.getId(), name);
        map.put("token", token);
        // 跳转到前端页面
        return "redirect:" + ConstantPropertiesUtil.YYGH_BASE_URL + "/weixin/callback?token="+map.get("token")+"&openid="+map.get("openid")+"&name="+URLEncoder.encode((String)map.get("name"));
    }

    /**
     * 获取微信登录参数
     */
    @GetMapping("getLoginParam")
    @ResponseBody
    public Result genQrConnect(HttpSession session) throws UnsupportedEncodingException {
        String redirectUri = URLEncoder.encode(ConstantPropertiesUtil.WX_OPEN_REDIRECT_URL, "UTF-8");
        Map<String, Object> map = new HashMap<>();
        map.put("appid", ConstantPropertiesUtil.WX_OPEN_APP_ID);
        map.put("redirectUri", redirectUri);
        map.put("scope", "snsapi_login");
        map.put("state", System.currentTimeMillis()+"");
        return Result.ok(map);
    }
}
