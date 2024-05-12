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
        .search-box, .search-button, .lucky-button {
            width: 300px;
            padding: 8px;
            margin-top: 10px;
            font-size: 16px;
            display: block;
            margin-bottom: 20px;
        }
        .buttons-container {
            display: flex;
            justify-content: center;
            margin-bottom: 20px;
        }
        .search-button, .lucky-button {
            width: auto; /* Adjust width to fit text */
            margin: 0 5px; /* Space between buttons */
        }
        h2, h3 {
            margin-bottom: 20px;
        }
    </style>
    <script>
        function validateForm() {
            var x = document.getElementById("searchQuery").value;
            if (x.trim() == "") {
                alert("Please enter a search term.");
                return false;
            }
            return true;
        }

        function submitSearch(actionPath) {
            var form = document.getElementById("searchForm");
            form.action = actionPath;
            form.submit();
        }
    </script>
</head>
<body>
<div class="centered-form">
    <h2>Search Engine</h2>
    <form id="searchForm" method="get" onsubmit="return validateForm()">
        <input type="text" id="searchQuery" name="Query" placeholder="Enter your search term" class="search-box">
        <div class="buttons-container">
            <button type="button" class="search-button" onclick="submitSearch('search')">Search</button>
            <button type="button" class="lucky-button" onclick="submitSearch('lucky')">I'm Feeling Lucky</button>
        </div>
    </form>
    <h3>Or</h3>
    <form action="keywordlist" method="get">
        <button type="submit" class="keyword-button">Browse and Search by Keywords</button>
    </form>
</div>
</body>
</html>
