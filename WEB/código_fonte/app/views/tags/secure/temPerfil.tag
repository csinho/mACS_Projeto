#{if controllers.Secure.autenticado() && controllers.Secure.temPerfil(_arg)}
    #{doBody /}
#{/if}