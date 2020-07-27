var counter;
var g_buttonClicked;
var submitTime = 0;
var activeRound = false;

//Warning on question refresh.
window.addEventListener('beforeunload', (event) => {
    if (activeRound === true) {
        event.preventDefault();
        event.returnValue = '';
    }
});

//Checking if round has begun.
var isRoundStartedRequest = $.ajax({

    url: "/api/isRoundStarted",
    method: "GET",
    dataType: "json",

    success: function (data) {
        if (data === false) {
            window.location = "Play.html";
        }
        else {
            newQuestion();
            activeRound = true;
        }
    }
});

function newQuestion() {
    var getQuestionRequest = $.ajax
        ({
            url: "/api/getQuestion",
            method: "GET",
            dataType: "json",


            success: function (data) {
                //Creating new timer for new question.
                $('#timer').svgTimer({

                    direction: 'forward',
                    transition: 'linear',
                    track: '#71E4D1',
                    fill: '#FFE4A4',
                    time: 10

                });

                document.getElementById("question").innerText = data.question;
                document.getElementById("ans1").textContent = data.answers[0];
                document.getElementById("ans2").textContent = data.answers[1];
                document.getElementById("ans3").textContent = data.answers[2];
                document.getElementById("ans4").textContent = data.answers[3];
                counter = data.counter;

                //Updating progress bar for each new question.
                document.getElementById("progressbar").style.width = 10 * (counter + 1) + "%";

                //Enabling buttons for new question.
                for (var i = 0; i < $(".btn-block").length; ++i) {
                    $(".btn-block")[i].disabled = false;
                }
            },
            error: function (data) {
                alert(data.message);

            }
        });
}

//Checking answer provided by user and updating relevant elements. 
function checkAnswer(buttonClicked) {
    //Obtaining time taken to answer question, refreshing timer after each check.
    var submittedTime = document.getElementById("timer").childNodes[0].childNodes[0].innerHTML;
    $("#timer").load(location.href + " #timer>*", "");

    //Disabling buttons once an answer has been selected.
    for (var i = 0; i < document.getElementsByClassName("button-custom3").length; ++i) {
        document.getElementsByClassName("button-custom3")[i].disabled = true;
    }

    g_buttonClicked = buttonClicked;

    var getCheckRequest = $.ajax({

        url: "/api/Check",
        method: "POST",
        data:
        {
            'submittedAns': document.getElementById(buttonClicked).textContent,
            'timer': submittedTime
        },
        dataType: "json",

        error: function (data) {
            alert(data.message);

        },

        success: function (data) {
            //Continue providing new questions until 10 total have been answered.
            if (counter < 9) {
                newQuestion();
            }
            else {
                //Ending round once 10 questions answered.
                activeRound = false;
                var endGameRequest = $.ajax({
                    url: "/api/endGame",
                    method: "POST",
                    dataType: "json",

                    error: function (data) {
                        alert(data.message);
                    },
                    success: function (data) {
                        //Dynamically creating results table and displaying them to user.
                        document.getElementById("questionForm").style.display = "none";
                        document.getElementById("question").innerText = "Round complete! You earned: " + data.totalPoints + " points."
                        document.getElementById("resultstable").style.display = "inline";
                        document.getElementById("progressbar-container").style.display = "none";
                        document.getElementById("home").style.display = "inline";
                        document.getElementById("playagain").style.display = "inline";

                        var parentEl = document.getElementById("resultstable");
                        var tablehead = document.createElement('thead');
                        var tabletr = document.createElement('tr');
                        var tableheading1 = document.createElement('th');
                        tableheading1.classList.add("tbl-head");
                        tableheading1.scope = "col";
                        tableheading1.innerHTML = "Category";
                        var tableheading2 = document.createElement('th');
                        tableheading2.classList.add("tbl-head");
                        tableheading2.style = "width:90%";
                        tableheading2.scope = "col";
                        tableheading2.innerHTML = "Question";
                        var tablebody = document.createElement('tbody');

                        //Creating row for every question in the round.
                        for (var i = 0; i < 10; ++i) {
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
                        };
                        //Appending tags to create table.
                        parentEl.appendChild(tablehead);
                        tablehead.appendChild(tabletr);
                        tabletr.appendChild(tableheading1);
                        tabletr.appendChild(tableheading2);
                        parentEl.appendChild(tablebody);
                    }
                });
            }
        }
    });
}