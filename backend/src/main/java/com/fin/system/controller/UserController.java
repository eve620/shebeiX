package com.fin.system.controller;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fin.system.commen.R;
import com.fin.system.entity.User;
import com.fin.system.entity.UserInfo;
import com.fin.system.service.UserService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

import static com.fin.system.commen.MD5Util.computeMD5Hash;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/whoami")
    public R<UserInfo> whoami(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userInfo") == null) {
            return R.error("用户未登录");
        }
        UserInfo user = (UserInfo) session.getAttribute("userInfo");
        return R.success(user);
    }

    //登录
    @PostMapping("/login")
    public R<User> login(HttpServletRequest request, @RequestBody User user) {
        System.out.println(request.getSession());
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
        UserInfo userInfo = new UserInfo(emp.getUserId(),emp.getUserAccount(), emp.getUserName(), emp.getRoleId());
        request.getSession().setAttribute("userInfo", userInfo);
        return R.success(emp);
    }

    @PostMapping("logout")
    public R<String> logout(HttpServletRequest request) {
        System.out.println(request.getSession());
        request.getSession().invalidate();
        return R.success("退出成功");
    }

    @GetMapping("/page")
    public R<List<User>> page(HttpServletRequest request, String input, boolean getAdmin) {
        //构造条件构造器
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper();
        //添加过滤条件
        queryWrapper.like(StringUtils.isNotEmpty(input), User::getUserName, input);
        //添加排序条件
        queryWrapper.orderByDesc(User::getUpdateTime);
        System.out.println(getAdmin);
        if(getAdmin){
            //添加查询条件
            queryWrapper.eq(User::getRoleId, 1);
        }else {
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
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper();
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
        userService.save(user);
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
        user.setUserPassword(computeMD5Hash(user.getUserPassword()));
        Boolean res = userService.updateById(user);
        if (res){
            return R.success("信息修改成功");
        }
        return R.error("信息修改失败");
    }

    //导出Excel
    @GetMapping("download")
    public void download(HttpServletResponse response, String name) throws IOException {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper();
        //添加过滤条件
        queryWrapper.like(StringUtils.isNotEmpty(name), User::getUserName, name);
        //添加排序条件
        queryWrapper.orderByDesc(User::getUpdateTime);

        List<User> data = userService.list(queryWrapper);
        // 这里注意 有同学反应使用swagger 会导致各种问题，请直接用浏览器或者用postman
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        String fileName = URLEncoder.encode("测试", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        EasyExcel.write(response.getOutputStream(), User.class).sheet("用户").doWrite(data);
    }
}
