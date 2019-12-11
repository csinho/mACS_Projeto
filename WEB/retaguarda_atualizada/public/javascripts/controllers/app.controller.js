(function () {
    'use strict';

    angular.module("app").controller("AppController", AppController);

    AppController.$inject = ['$uibModal', '$scope', '$http', 'Paginacao', '$filter', '$window'];

    function AppController($uibModal, $scope, $http, Paginacao, $window, $filter) {
        var me = this;

        me.adicionar = adicionar;
        me.estaEditandoAdicionando = estaEditandoAdicionando;
        me.iniciar = iniciar;
        me.cancelar = cancelar;
        me.salvar = salvar;
        me.editar = editar;
        me.carregarTela = carregarTela;        
        me.openModelToConfirmDelete = openModelToConfirmDelete;       
        me.cancelarExclusao = cancelarExclusao;
        me.deleteApp = deleteApp;      
        
        me.update = update;

        me.adicionando = 'adicionando';
        me.editando = 'editando';
        me.statusTela = null;
        me.adicionando = false;
        me.campoBusca = null;

        // Start de funções
        me.iniciar();       
        me.app = {};

        function iniciar() {
            me.app = {};
            me.statusTela = null;

            me.paginador = Paginacao.getPaginador(function (pagina) {
                me.carregarTela(pagina);
            });

            me.carregarTela(1);
        }

        function carregarTela(pagina) {
            me.paginador.paginaAtual = pagina;

            $http.get("/apps/list", {
                params: {
                    id: me.idAt,
                    pagina: me.paginador.paginaAtual,
                    porPagina: me.paginador.porPagina,
                    versionDesc: me.campoBusca ? me.campoBusca : ''
                }
            }).success(function (resultado) {
                me.apps = resultado.data;
                me.paginador.calcularNumeroPaginas(resultado.total);
            }).error(function (resultado) {
                toastr.error("Problema ao carregar apps")
            });
        }
        

        function update(){
            $http.post("/apps/update", {
                params:  {apps: me.appsToUpdate}
            }).success(function (resultado) {
               toastr.success("App(s) atualizada(s) com sucesso");
            }).error(function (resultado) {
                toastr.error("Problema ao atualizar apps")
            });
        }


        function openModelToConfirmDelete(app) {           
            me.app = app;
            me.modalConfirmarExcluirApp = $uibModal.open({
                templateUrl: "modalConfirmarExcluirApp.html",
                scope: $scope
            });
        }


        function adicionar() {
            me.statusTela = me.adicionando;
        }

        function editar(app) {
            me.app = angular.copy(app);
            me.statusTela = me.editando;
        }

        function cancelar() {
            me.iniciar();
        }

        function estaEditandoAdicionando() {
            return me.statusTela == me.editando || me.statusTela == me.adicionando;
        }

        function salvar() {
            // Isso ainda precisa ser filtrado!
            if ($('.ng-invalid-required').length > 0) {
                toastr.warning("Campos obrigatórios sem preenchimento!");
                return;
            }            
            me.app.versionDate = null;
            me.app.version = me.app.version.trim().replace('_','');
            $http.post("/apps/", JSON.stringify(me.app))
            .success(function (data) {
                toastr.success("A App foi salva com sucesso");
                me.iniciar();
            })
            .error(function (data) {
                toastr.error(data.msg);
            });
        }


        function cancelarExclusao() {
            me.usuario = null;
            me.modalConfirmarExcluirApp.close();
        }

        function deleteApp() {
            $http.delete("/apps/" + me.app.id)
                .success(function (data) {
                    me.iniciar();
                    toastr.success("App excluída com sucesso");
                    me.modalConfirmarExcluirApp.close();
                })
                .error(function () {
                    toastr.error("Houve problema ao excluir a App");
                    me.modalConfirmarExcluirApp.close();
                });
        }

        me.dismiss = function () {
            me.modalConfirmarExcluirApp.close();
        };
    }

})();