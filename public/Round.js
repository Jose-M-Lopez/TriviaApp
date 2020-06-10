var cntr = 0;
var g_buttonClicked;
var submitTime = 0;
var activeRound = false;

window.addEventListener('beforeunload', (event) => {
    if (activeRound === true) {
        // Cancel the event as stated by the standard.
        event.preventDefault();
        // Chrome requires returnValue to be set.
        event.returnValue = '';
    }
});

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

                $('#timer').svgTimer({

                    direction: 'forward',
                    transition: 'linear',
                    track: '#71E4D1',
                    fill: '#FFE4A4',
                    time: 10

                });


                document.getElementById("question").innerHTML = data.question;
                document.getElementById("ans1").textContent = data.answers[0];
                document.getElementById("ans2").textContent = data.answers[1];
                document.getElementById("ans3").textContent = data.answers[2];
                document.getElementById("ans4").textContent = data.answers[3];

                for (var i = 0; i < $(".btn-block").length; ++i) {
                    $(".btn-block")[i].disabled = false;
                }
            },
            error: function (data) {
                alert(data.message);

            }
        });
}

function checkAnswer(buttonClicked) {
    var submittedTime = document.getElementById("timer").childNodes[0].childNodes[0].innerHTML;
    $("#timer").load(location.href + " #timer>*", "");

    for (var i = 0; i < document.getElementsByClassName("button-custom3").length; ++i) {
        document.getElementsByClassName("button-custom3")[i].disabled = true;
    }

    g_buttonClicked = buttonClicked;
    var oldWidth = parseInt(document.getElementById("progressbar").style.width);
    document.getElementById("progressbar").style.width = oldWidth + 10 + "%";

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
            if (cntr < 9) {
                ++cntr;
                newQuestion();
            }
            else {
                activeRound = false;
                var endGameRequest = $.ajax({
                    url: "/api/endGame",
                    method: "POST",
                    dataType: "json",

                    error: function (data) {
                        alert(data.message);
                    },
                    success: function (data) {
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

                        for (var i = 0; i < 10; ++i) {
                            var questiontr = document.createElement('tr');

                            var questioncategory = document.createElement('td');
                            questioncategory.setAttribute("id", "c" + i);
                            questioncategory.setAttribute("scope", "row");
                            questioncategory.innerHTML = data.roundQuestions[i].category;

                            var questioncontent = document.createElement('td');
                            questioncontent.setAttribute("id", "q" + i);
                            questioncontent.setAttribute("scope", "row");
                            questioncontent.innerHTML = data.roundQuestions[i].question;



                            tablebody.appendChild(questiontr);
                            questiontr.appendChild(questioncategory);
                            questiontr.appendChild(questioncontent);

                            if (data.roundQuestions[i].ifCorrect === false) {
                                questioncategory.style.backgroundColor = "#FFB1BD";
                                questioncontent.style.backgroundColor = "#FFB1BD";
                            }
                            else {
                                questioncategory.style.backgroundColor = "#7FE6A1";
                                questioncontent.style.backgroundColor = "#7FE6A1";
                            }


                        };
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