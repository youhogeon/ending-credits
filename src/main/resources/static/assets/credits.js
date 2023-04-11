window.addEventListener("load", (event) => {
    let height = document.body.scrollHeight - window.innerHeight;

    setTimeout(() => {
        window.scrollTo(0, 0);
    }, 300);

    setTimeout(() => {
        const timer = setInterval(() => {
            window.scrollBy(0, 1);
            if (window.pageYOffset >= height) {
                clearInterval(timer);
                console.log("done")
            }
        }, 12);
    }, 500);
});