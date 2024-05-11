<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>Search Engine</title>
    <style>
        .centered-form {
            position: absolute;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            text-align: center;
        }
        .search-box {
            width: 80%;
            padding: 12px 20px;
            font-size: 16px;
        }
        h2 {
            margin-bottom: 20px;
        }
    </style>
    <script>
        function validateForm() {
            var x = document.forms["searchForm"]["Query"].value;
            if (x.trim() == "") {
                alert("Please enter a search term.");
                return false;
            }
            return true;
        }
    </script>
</head>
<body>
<div class="centered-form">
    <h2>Search Engine</h2>
    <form name="searchForm" action="search" method="get" onsubmit="return validateForm()">
        <input type="text" name="Query" placeholder="Enter your search term" class="search-box">
        <button type="submit" style="display:none;">Search</button>
    </form>
</div>
</body>
</html>
