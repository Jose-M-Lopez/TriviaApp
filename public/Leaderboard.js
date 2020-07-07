//Altering leaderboard display depending on whether or not user is logged in.
checkLogin().then(function (isLoggedIn) {
    if (isLoggedIn === false) {
        $('#login').hide();
        $('#play').hide();
        document.getElementById('logout').onclick = function () {
            window.location = "Login.html";
            //return false;
        };
        document.getElementById('logout').innerHTML = "Login";
        $('#matchHistory').hide();
    }
});

//Request to obtain leaderboard information from back-end.
var getLeaderboardRequest = $.ajax(
    {
        url: "/api/Leaderboard",
        method: "GET",
        dataType: "json",

        error: function (data) {
            alert(data.message);
        },
        //Dynamically creating and populating leaderboard table. 
        success: function (data) {
            var parentEl = document.getElementById('Leaderboard-container');

            var table = document.createElement('table');
            table.setAttribute("id", "leadertable");
            table.classList.add("cstm-tbl", "table-responsive", "table-bordered", "table-small", "table", "table-dark");
            table.setAttribute("style", "overflow-x:auto;");

            var tablehead = document.createElement('thead');

            var headtr = document.createElement('tr');

            var tableheading1 = document.createElement('th');
            tableheading1.setAttribute("scope", "col");
            tableheading1.classList.add("tbl-head");
            tableheading1.innerHTML = "Player";
            tableheading1.setAttribute("style", "width:86%;");

            var tableheading2 = document.createElement('th');
            tableheading2.setAttribute("scope", "col");
            tableheading2.classList.add("tbl-head");
            tableheading2.innerHTML = "Lifetime Points";

            var tablebody = document.createElement('tbody');

            //Creating row for each of the top players.
            for (var i = 0; i < data.topPlayers.length; ++i) {
                var playertr = document.createElement('tr');

                var playername = document.createElement('td');
                playername.setAttribute("id", "u" + i);
                playername.setAttribute("scope", "row");
                playername.innerHTML = data.topPlayers[i].username;

                var playerpoints = document.createElement('td');
                playerpoints.setAttribute("id", "p" + i);
                playerpoints.setAttribute("scope", "row");
                playerpoints.innerHTML = data.topPlayers[i].points;

                //Even and odd row color stylization.
                if (i % 2) {
                    playerpoints.style.backgroundColor = "#7FE6A1";
                    playername.style.backgroundColor = "#7FE6A1";
                }
                else {
                    playerpoints.style.backgroundColor = "#67CFEE";
                    playername.style.backgroundColor = "#67CFEE";
                }
                tablebody.appendChild(playertr);
                playertr.appendChild(playername);
                playertr.appendChild(playerpoints);
            }
            //Appending tags to create table.
            parentEl.appendChild(table);
            table.appendChild(tablehead);
            tablehead.appendChild(headtr);
            headtr.appendChild(tableheading1);
            headtr.appendChild(tableheading2);
            table.appendChild(tablebody);
        }
    });