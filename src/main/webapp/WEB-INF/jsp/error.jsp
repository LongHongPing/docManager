<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta charset="utf-8">
    <title>错误声明页面</title>
    <link href="css/pintuer.css" rel="stylesheet"/>
    <style>
        *{ margin:0; padding:0; list-style:none;}
        table{border-collapse:collapse;border-spacing:0;}
        body,html{ height:100%; font-family:'微软雅黑'; overflow-y:hidden;}
        .main{ width:60%; margin-left:20%; margin-right:20%; margin-top:10%;}
        .main_left{ width:38%; margin-left:12%; margin-top:10%; float:left;}
        .main_right{width:50%; float:left;}
        .main_radius{ padding-top:4%; width:75%; height:130px; border-radius:50%; background:#fef2ec; font-size:18px;text-align:center;}
        .main_p{ font-family:'华文行楷';}
    </style>
</head>

<body>
<div class="main">
    <div class="main_left"><img src="images/img2.png" width="229" height="128"/></div>
    <div class="main_right">
        <div class="main_radius">
            <p class="main_p">执行失败</p>
            <p class="main_p">请详细检查行为是否正确！</p>
        </div>
        <div class="text-left" style="margin-top:10%; margin-left:8%;">
            <a  class="button" href="${pageContext.request.contextPath}/index">返回</a>
        </div>
    </div>
</div>
</body>
</html>