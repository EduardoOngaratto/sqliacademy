// Lab 3: demonstracao (depois de resolvido) do ataque do Lab 1 contra as duas versoes.
// So exibicao, nada e enviado ao servidor. Texto digitado entra via textContent (nunca innerHTML).
(function () {
    const usuario = document.getElementById('demo-usuario');
    if (!usuario) {
        return; // lab ainda nao resolvido: a demonstracao nao e exibida
    }
    const senha = document.getElementById('demo-senha');
    const vulneravel = document.getElementById('demo-vulneravel');
    const param1 = document.getElementById('demo-param1');
    const param2 = document.getElementById('demo-param2');

    function trecho(texto, digitado) {
        const elemento = document.createElement(digitado ? 'mark' : 'span');
        elemento.textContent = texto;
        return elemento;
    }

    function atualizar() {
        // Mesmo modelo do Lab 1 (lab1.js / Lab1Validador)
        vulneravel.replaceChildren(
            trecho("SELECT * FROM usuario WHERE login = '"),
            trecho(usuario.value, true),
            trecho("' AND senha = '"),
            trecho(senha.value, true),
            trecho("'"));
        // Na versao segura o valor vai inteiro, como dado
        param1.replaceChildren(trecho(usuario.value, true));
        param2.replaceChildren(trecho(senha.value, true));
    }

    usuario.addEventListener('input', atualizar);
    senha.addEventListener('input', atualizar);
    atualizar();
})();
