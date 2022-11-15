addmodal = null
// 画面読み込み時
window.onload = function() {
    addmodal = 
    $("#openAddTodoDialog").on("click",function(){
        $('#addmodal').show()
    })
    $(".addmodal-close-func").on("click",function(){
        $('#addmodal').hide()
    })
}