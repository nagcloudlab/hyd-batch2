



//design issues
// code tangling / tight coupling
// code scattering / code duplication

function hello(){
    console.log("hello")
}

function hi(){
    console.log("Hi")
}

function bye(){
    console.log("Bye")
}


function withEmoji(f){
    return function(){
        f();
        console.log("😀")
    }
}

hello();
withEmoji(hello)()