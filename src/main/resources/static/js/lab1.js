// Lab 1: monta a consulta em tempo real enquanto o aluno digita.
// So exibicao: a validacao de verdade e feita no servidor (Lab1Validador).
// O texto digitado entra via textContent (nunca innerHTML), para nao abrir brecha de XSS.
(function () {
    const usuario = document.getElementById('usuario');
    const senha = document.getElementById('senha');
    const preview = document.getElementById('query-preview');

    function trecho(texto, digitado) {
        const elemento = document.createElement(digitado ? 'mark' : 'span');
        elemento.textContent = texto;
        return elemento;
    }

    // Mesmo modelo de Lab1Validador
    function atualizar() {
        preview.replaceChildren(
            trecho("SELECT * FROM usuario WHERE login = '"),
            trecho(usuario.value, true),
            trecho("' AND senha = '"),
            trecho(senha.value, true),
            trecho("'"));
    }

    usuario.addEventListener('input', atualizar);
    senha.addEventListener('input', atualizar);
    atualizar();
})();
