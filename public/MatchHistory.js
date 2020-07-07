//RoundID keys mapped to points earned value.
var roundPoints = new Map();

//Redirecting to homepage if not logged in.
checkLogin().then(function test(isLoggedIn) {
    if (isLoggedIn === false) {
        window.location = "/";
    }
    else {
        var matchRoundRequest = $.ajax(
            {
                url: "/api/roundHistory",
                method: "GET",
                dataType: "json",

                error: function (data) {
                    alert(data.message);
                },
                //Populating selection form with all the users matches. 
                success: function (data) {
                    var rounds = [];
                    var parentEl = document.getElementById('matchSelect');
                    var roundNum = 1;

                    data.roundList.forEach(function (round) {
                        //Inserting RoundID and the points earned for that round into map.
                        rounds.push(round.RoundID);
                        roundPoints.set(round.RoundID, round.totalPoints);
                    });

                    for (var i = 0; i < rounds.length; ++i) {
                        childEl = document.createElement('option');
                        childEl.setAttribute("value", rounds[i]);
                        childEl.innerText = "Match: #" + roundNum;

                        parentEl.appendChild(childEl);

                        roundNum = roundNum + 1;
                    }
                }
            });
    }
});

//Helper function that dynamically generates history table once a match is selected.
function displayResults(data) {
    var roundID = data.value;
    var points = roundPoints.get(Number(roundID));

    //Removing old table upon the selection of a new match.
    $("#resultstable").remove();

    var roundQuestionRequest = $.ajax(
        {
            url: "/api/questionHistory",
            method: "GET",
            data: {
                'RoundID': roundID,
            },
            dataType: "json",

            success: function (data) {
                var parentEl = document.getElementById('History-container');

                var table = document.createElement('table');
                table.setAttribute("id", "resultstable");

                table.classList.add("cstm-tbl", "table-responsive", "table-small", "table", "table-dark");
                table.setAttribute("style", "overflow-x:auto;");

                var tablehead = document.createElement('thead');

                var headtr = document.createElement('tr');

                var tableheading1 = document.createElement('th');
                tableheading1.setAttribute("scope", "col");
                tableheading1.classList.add("tbl-head");
                tableheading1.innerHTML = "Category";

                var tableheading2 = document.createElement('th');
                tableheading2.setAttribute("scope", "col");
                tableheading2.classList.add("tbl-head");
                tableheading2.innerHTML = "Question";
                tableheading2.setAttribute("style", "width:90%;");

                var tablebody = document.createElement('tbody');

                //Creating row for each question of the round.
                for (var i = 0; i < data.roundQuestions.length; ++i) {
                    var questiontr = document.createElement('tr');

                    var questioncategory = document.createElement('td');
                    questioncategory.setAttribute("id", "c" + i);
                    questioncategory.setAttribute("scope", "row");
                    questioncategory.innerText = data.roundQuestions[i].category;

                    var questioncontent = document.createElement('td');
                    questioncontent.setAttribute("id", "q" + i);
                    questioncontent.setAttribute("scope", "row");
                    questioncontent.innerText = data.roundQuestions[i].question;

                    tablebody.appendChild(questiontr);
                    questiontr.appendChild(questioncategory);
                    questiontr.appendChild(questioncontent);

                    //Coloring row red if answered incorrectly, green if correctly.
                    if (data.roundQuestions[i].ifCorrect === false) {
                        questioncategory.style.backgroundColor = "#FFB1BD";
                        questioncontent.style.backgroundColor = "#FFB1BD";
                    }
                    else {
                        questioncategory.style.backgroundColor = "#7FE6A1";
                        questioncontent.style.backgroundColor = "#7FE6A1";
                    }
                }
                var difficultytr = document.createElement('tr');
                var pointstr = document.createElement('tr');

                var difficultyoption = document.createElement('td');
                difficultyoption.innerText = "Difficulty: " + data.roundQuestions[0].difficulty.toUpperCase();
                difficultyoption.setAttribute("colspan", "2");
                difficultyoption.style.textAlign = "left";
                difficultyoption.style.textIndent = "20px";
                difficultyoption.style.backgroundColor = "#FFE4A4";

                var roundpoints = document.createElement('td');
                roundpoints.innerText = "Points Earned: " + points;
                roundpoints.setAttribute("colspan", "2");
                roundpoints.style.textAlign = "left";
                roundpoints.style.textIndent = "20px";
                roundpoints.style.backgroundColor = "#FFE4A4";

                //Appending tags to create table.
                difficultytr.appendChild(difficultyoption);
                pointstr.appendChild(roundpoints);
                tablebody.appendChild(difficultytr);
                tablebody.appendChild(pointstr);
                parentEl.appendChild(table);
                table.appendChild(tablehead);
                tablehead.appendChild(headtr);
                headtr.appendChild(tableheading1);
                headtr.appendChild(tableheading2);
                table.appendChild(tablebody);
            }
        });
}