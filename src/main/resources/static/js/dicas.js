// Dicas progressivas dos laboratorios: revela uma por clique
(function () {
    const botao = document.getElementById('botao-dica');
    const dicas = document.querySelectorAll('[data-dica]');
    let mostradas = 0;

    botao.addEventListener('click', function () {
        if (mostradas < dicas.length) {
            dicas[mostradas].classList.remove('d-none');
            mostradas++;
        }
        botao.textContent = 'Mostrar dica (' + mostradas + ' de ' + dicas.length + ')';
        botao.disabled = mostradas === dicas.length;
    });
})();
