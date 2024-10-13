package com.fin.system.controller;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fin.system.commen.R;
import com.fin.system.entity.User;
import com.fin.system.entity.UserInfo;
import com.fin.system.service.UserService;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.bouncycastle.util.encoders.Base64Encoder;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.List;
import java.util.Map;

import static com.fin.system.commen.MD5Util.computeMD5Hash;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;
    private final static String servletSessionCookieName = "JSESSIONID";
    private final static String casLogoutUrl = "https://ids.xidian.edu.cn/authserver/";
    private final static String casApplicationUrl = URLEncoder.encode("https://shebei.xidian.edu.cn/login", StandardCharsets.UTF_8);
    private final static String casLogoutRedirectUrl = casLogoutUrl + "logout?service=" + casApplicationUrl;
    public static String filenameEncoding(String filename, HttpServletRequest request) throws UnsupportedEncodingException {
        // 获得请求头中的User-Agent
        String agent = request.getHeader("User-Agent");
        // 根据不同的客户端进行不同的编码

        if (agent.contains("MSIE")) {
            // IE浏览器
            filename = URLEncoder.encode(filename, StandardCharsets.UTF_8);
        }  else {
            // 其它浏览器
            filename = URLEncoder.encode(filename, StandardCharsets.UTF_8);
        }
        return filename;
    }
    @GetMapping("/whoami")
    public R<UserInfo> whoami(HttpServletRequest request) throws UnsupportedEncodingException {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("userInfo") != null) {
            return R.success((UserInfo) session.getAttribute("userInfo"));
        }
        String userId = request.getRemoteUser();
        if (userId == null) {
            return R.error("用户未登录");
        }
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserAccount, userId);
        User emp = userService.getOne(queryWrapper);
        if (emp == null) {
            Principal principal = request.getUserPrincipal();
            if (principal instanceof AttributePrincipal aPrincipal) {
                User user = new User();
                Map<String, Object> map = aPrincipal.getAttributes();
                String cn = new String(map.get("cn").toString().getBytes(Charset.forName("GBK")), StandardCharsets.UTF_8);
                user.setUserAccount(userId);
                user.setUserName(cn);
                user.setRoleId(0);
                user.setUserPassword(computeMD5Hash("123456"));
                userService.save(user);
                UserInfo userInfo = new UserInfo(user.getUserId(), user.getUserAccount(), user.getUserName(), user.getRoleId());
                if (session != null) {
                    session.setAttribute("userInfo", userInfo);
                }
                return R.success(userInfo);
            }
            return R.error("信息验证错误");
        }
        UserInfo userInfo = new UserInfo(emp.getUserId(), emp.getUserAccount(), emp.getUserName(), emp.getRoleId());
        request.getSession().setAttribute("userInfo", userInfo);
        return R.success(userInfo);
    }

    //登录
    @PostMapping("/login")
    public R<User> login(HttpServletRequest request, @RequestBody User user) {
        String password = user.getUserPassword();
        //md5处理
        password = computeMD5Hash(password);
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserAccount, user.getUserAccount());
        User emp = userService.getOne(queryWrapper);
        if (emp == null) {
            return R.error("用户不存在");
        }
        if (!emp.getUserPassword().equals(password)) {
            return R.error("密码错误");
        }
        UserInfo userInfo = new UserInfo(emp.getUserId(), emp.getUserAccount(), emp.getUserName(), emp.getRoleId());
        request.getSession().setAttribute("userInfo", userInfo);
        return R.success(emp);
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) throws
            ServletException, IOException {
//        request.getSession().invalidate();
//        return R.success("退出成功");
        var casLoaded = request.getRemoteUser() != null;
        var session = request.getSession(false);
        if (session != null) {
            session.invalidate();
            // 销毁cookie
            destroySessionCookie(response);
        }
        // 如果需要重定向，进行重定向
        if (casLoaded) {
            response.sendRedirect(casLogoutRedirectUrl);
        }
    }

    private void destroySessionCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(servletSessionCookieName, null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    @GetMapping("/page")
    public R<List<User>> page(HttpServletRequest request, String input, boolean getAdmin) {
        //构造条件构造器
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper();
        //添加过滤条件
        queryWrapper.like(StringUtils.isNotEmpty(input), User::getUserName, input);
        //添加排序条件
        queryWrapper.orderByDesc(User::getUpdateTime);
        if (getAdmin) {
            //添加查询条件
            queryWrapper.eq(User::getRoleId, 1);
        } else {
            //添加查询条件
            queryWrapper.eq(User::getRoleId, 0);
        }
        //执行查询
        List<User> users = userService.list(queryWrapper);
        return R.success(users);
    }

    @GetMapping("/list")
    public R<List<User>> list() {
        //构造条件构造器
        QueryWrapper<User> queryWrapper = new QueryWrapper();
        queryWrapper.select("user_name", "user_account");
        List<User> userList = userService.list(queryWrapper);
        return R.success(userList);
    }

    //根据id查询教师信息
    @GetMapping("/{id}")
    public R<User> getById(@PathVariable Long id) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserId, id);
        User user = userService.getOne(queryWrapper);
        if (user != null) {
            return R.success(user);
        }
        return R.error("没有查询到对应教师信息");
    }

    //新增教师
    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody User user) {
        user.setUserPassword(computeMD5Hash(user.getUserPassword()));
        userService.save(user);
        if (user.getRoleId() == 1) {
            return R.success("新增管理员成功");
        }
        return R.success("新增教师成功");
    }

    //删除教师
    @DeleteMapping("/{id}")
    public R<String> deleteById(@PathVariable Long id) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserId, id);
        Boolean res = userService.remove(queryWrapper);
        if (res) {
            return R.success("用户删除成功");
        }
        return R.error("用户删除失败");
    }

    //编辑信息
    @PutMapping
    public R<String> update(HttpServletRequest request, @RequestBody User user) {
        if (user.getUserPassword() != null) {
            user.setUserPassword(computeMD5Hash(user.getUserPassword()));
        }
        //更新session，用以更新顶部栏
        UserInfo before = (UserInfo) request.getSession().getAttribute("userInfo");
        if (before.userId == user.getUserId()) {
            request.getSession().setAttribute("userInfo", new UserInfo(user.getUserId(), user.getUserAccount(), user.getUserName(), user.getRoleId()));
        }
        Boolean res = userService.updateById(user);
        if (res) {
            return R.success("信息修改成功");
        }
        return R.error("信息修改失败");
    }

    //导出Excel
    @PostMapping("/download")
    public void download(HttpServletResponse response, @RequestBody List<User> users) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        try (OutputStream outputStream = response.getOutputStream()) {
            EasyExcel.write(outputStream, User.class).sheet("users").doWrite(users);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
