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
            width: 300px;
            padding: 8px;
            margin-top: 10px;
            font-size: 16px;
            display: block;
            margin-bottom: 20px;
        }
        h2, h3 {
            margin-bottom: 20px;
        }
    </style>
    <script>
        function validateForm() {
            var x = document.forms["searchForm"]["Query"].value;
            if (x.trim() == "") {
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
    <h3>Or</h3>
    <form action="keywordlist" method="get">
        <button type="submit" class="keyword-button">Browse and Search by Keywords</button>
    </form>
</div>
</body>
</html>
