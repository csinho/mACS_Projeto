;(function () {
  'use strict';

  angular.module('app').controller('FormController', FormController);

  FormController.$inject = ['FormService', 'Paginacao', '$filter', '$window','$scope', '$http'];

  function FormController (FormService, Paginacao, $filter, $window, $scope, $http) {
    var me = this;
    me.forms = [];
    me.fields = [];
    me.operators = [];
    me.filter = {};
    me.filters = [];
    me.results = [];
    me.view = 'search';
    me.viewFilters = [];
    me.fieldDataType = null;
    me.formVersion = 1;
    me.fieldDomainOptions = [];
    me.windowTitle = 'Consultar formulários';
    me.currentUser = null;
    me.showCity = false;
    me.reevaluateDisableUnityFilter = true;
    me.reevaluateDisableCityFilter = true;
    me.searchUserPath = '';
    me.selectedForms = [];
    me.cityUrlParam = '';
    me.enabledExport = false;
    
    /**
     * Starts the controller running the initial actions like getting the current user data
     */
    me.activate = function () {
      me.paginator = Paginacao.getPaginador(function (page) {
        me.search(page);
      }, 10);
      FormService.getAllTemplates().then(
        function (response) {
          me.formTemplates = response.data;
        }
      );
      FormService.getCurrentUser().success(function (user) {
        me.currentUser = user;
        if (me.currentUser.perfis !== 'usuario' &&  me.formTemplate && me.formTemplate.isExportable === true) {
          me.enabledExport = true;
        }
        me.userCity = me.currentUser.userData.municipio;
        if (me.userCity && me.userCity.nom_municipio) {
          me.userCity.nome = me.userCity.nom_municipio;
        }
        me.userUnity = me.currentUser.userData.unidadeSaude;
        reevaluateDisableCityFilter();
        reevaluateDisableUnityFilter();
        updateUserSearchPath();
      });
    };

    /**
     * Loads the domain options (if applicable) when a field is selected,
     * the operators available to the field and set the fieldDataType
     * @param  {} field
     */
    me.fieldChanged = function (field) {
      me.filter.value = null;
      me.fieldDataType = null;
      FormService.getDomainOptions(me.filter.field.slug).then(function (response) {
        if (response && response.status == 200 && response.data.length > 0) {
          me.fieldDataType = 'domaindata';
          me.fieldDomainOptions = response.data;
        }else {
          me.fieldDataType = me.filter.field.dataType;
        }
        FormService.getOperators(me.fieldDataType).then(function (response) {
          me.operators = response.data;
          if (me.operators.length === 1) {
            me.filter.operator = me.operators[0];
          }
        });
      });
    };

    /**
     * Reset the filter value when the operator change
     */
    me.operatorChanged = function () {
      me.filter.value = null;
    };

    /**
     * Load the fields vbased in the template selected and reset the filters
     */
    me.formTemplateTypeChanged = function () {
      me.clear();      
      FormService.getFields(me.formTemplate.id).then(function (response) {
        me.fields = response.data;
      });
    };

    /**
     * Clears the form filters
     */
    me.clear = function () {
      me.filter = {};
      me.filters = [];
      me.viewFilters = [];
    };

    /**
     * Add a filter to filter colletion
     */
    me.addFilter = function () {
      FormService.appendFilter(me.filter, me.filters, me.viewFilters, me.fieldDataType)
      // reset filter
      me.filter.value = null;
    };

    /**
     * Removes a filter based in its index 
     * @param  {} filterIndex
     */
    me.removeFilter = function (filterIndex) {
      for (var index = 0; index < me.filters.length; index++) {
        if (index === filterIndex) {
          if (index > 0) {
            me.filters.splice(index + 1, 2);
          }else {
            me.filters.splice(index, 2);
          }
          me.viewFilters.splice(index, 1);
          return;
        }
      }
    };

    /**
     * Searchs forms in service based in the filters/criterias selected
     * The date filters are adjusted/formatted to be in accordance with the expectations of the back end service
     * The formQuery object contains all the informations defined to filter forms, inclusing the filters colelction,
     * the current page, the formVersion, the form date of creation and synchronization, the form type.
     * Data about the user that has created the form can also be used to filter them, 
     * like user unity, user city, and the user itself (deviceUser)
     * The filters applied based in any form field are also in a couple to describe the form field itself and the form field value
     */
    me.search = function (page) {
    	
	if (!page) page = 1;
      me.formQuery = {
        'filters': me.filters,
        'page': page,
        'formVersion': me.formTemplate.formVersion,
        'formType': me.formTemplate.slug,
        'formCreatedAtFrom': $filter('date')(me.formCreatedAtFrom, 'yyyy-MM-dd'),
        'formCreatedAtTo': $filter('date')(me.formCreatedAtTo, 'yyyy-MM-dd'),
        'formSyncedAtFrom': $filter('date')(me.formSyncedAtFrom, 'yyyy-MM-dd'),
        'formSyncedAtTo': $filter('date')(me.formSyncedAtTo, 'yyyy-MM-dd'),
        'listOldRevisions': me.listOldRevisions
      };
      if (me.unity) {
        me.formQuery.unityId = me.unity.id;
      }
      if (me.city) {
        me.formQuery.cityId = me.city.id;
      }
      if (me.deviceUser) {
        me.formQuery.deviceUserId = me.deviceUser.id;
      }
      console.log(me.formQuery);
      FormService.search(me.formQuery).then(
        function (response) {
        	
          me.results = response.data.lista;
          me.paginator.paginaAtual = me.formQuery.page;
          me.paginator.calcularNumeroPaginas(response.data.total);
          me.windowTitle = 'Formulários encontrados';
          me.windowTitle += me.listOldRevisions ? ' (com versões antigas)' : '';

          if (me.results.length === 0) {
            toastr.warning('Nenhum registro encontrado');
          }else {
            me.view = 'list';
          }
          me.enabledExport = me.formTemplate.isExportable;
          $window.scrollTo(0, 0);
        },
        function (response) {
          toastr.error(response.data);
        }
      );
    };

    

    /**
     * Gets amd show the form details based in the formId
     * @param  {} idForm
     */
    me.showFormDetailsByFormId = function (idForm) {
      me.windowTitle = 'Formulários encontrados';
      me.formRevisions = [];
      FormService.getFormSetByFormId(idForm).then(function (formSet) {
        me.formSet = formSet;
        me.view = 'detail';
        populateFormRevisions(me.formSet.form.slug, me.formSet.form.id);
      });
    };

    /**
     * Close the form details and change the view state to list
     */
    me.closeDetail = function () {
      me.view = 'list';
      $window.scrollTo(0, 0);
    };

    /**
     * Prints the current form detail page
     */
    me.print = function () {
      $window.print();
    };

    /**
     * Gets amd show the form details based in the formSlug
     * @param  {} formSlug
     */
    me.showFormDetailsByFormDataRef = function (formData) {
      me.windowTitle = 'Formulários encontrados';
      me.formRevisions = [];
      FormService.getFormSetByFormSlugAndDate(formData.value, formData.targetReferenceCreatedAt).then(function (formSet) {
        me.formSet = formSet;
        me.view = 'detail';
        populateFormRevisions(me.formSet.form.slug, me.formSet.form.id);
      });
    };

    /**
     * Return the view state to form search
     */
    me.back = function () {
      me.view = 'search';
      me.results = [];
      me.windowTitle = 'Consultar formulários';
    };

    /**
     * Reset the unity filter when a city is typed and selected in the suggestion component
     */
    me.cityChanged = function () {
      if (me.city !== null && me.city !== undefined) {
        me.unity = null;
      }
    };

    /**
     * Updates the user filter params when the unity is typed and selected in the suggestion component
     */
    me.unityChanged = function () {
      if (me.unity) {
        me.deviceUser = null;
        updateUserSearchPath();
      }
    };

    /**
     * Reevaluate the disable city filter
     */
    function reevaluateDisableCityFilter () {
      me.disableCityFilter = !(me.currentUser.perfis === 'gestor_estado' || me.currentUser.perfis === 'administrador');
      if (me.disableCityFilter && me.userCity) {
        me.city = angular.copy(me.userCity);
        me.cityUrlParam = '/' + me.city.id;
      }
    }

    /**
     * Reevaluate the disable unity filter
     */
    function reevaluateDisableUnityFilter () {
      me.disableUnityFilter = !(me.currentUser.perfis === 'gestor_municipio' || me.currentUser.perfis === 'gestor_estado' || me.currentUser.perfis === 'administrador' && me.userCity !== null && me.userCity !== undefined)
      if (me.disableUnityFilter && me.userUnity) {
        me.unity = angular.copy(me.userUnity);
      }
    }

    /**
     * Updates the user search path based in the unity
     */
    function updateUserSearchPath () {
      me.searchUserPath = '/usuarios/search?';
      if( (me.userUnity && me.userUnity.id)) {
        me.searchUserPath += 'unityId=' + me.userUnity.id + '&term=';
      }
      else if( (me.unity && me.unity.id)) {
        me.searchUserPath += 'unityId=' + me.unity.id + '&term=';
      }else {
        me.searchUserPath += '&term=';
      }
      me.searchUserDisabled = false;
    }

    /**
     * Get the selected forms
     */
    function getSelectedItens () {
      var selectedForms = [];
      for (var index = 0; index < me.results.length; index++) {
        if (me.results[index].selected) {
          var form = me.results[index];
          selectedForms.push(form.id);
        }
      }
      return selectedForms;
    }

    /**
     * Count the selected forms
     */
    me.countSelectedForms = function () {
      return getSelectedItens().length;
    };

    /**
     * Exports the selected forms
     */
    me.exportSelected = function () {
      me.selectedForms = getSelectedItens();
      if (me.selectedForms.length === 0) {
        toastr.error('Nenhum item selecionado para exportar');
      }else {
        var formExport = {formsId: me.selectedForms};
        FormService.exportSelected(formExport).then(
          function (response) {
            if (response.data != 'false') {
              window.location.href = response.data;
              toastr.success('Dados exportados com sucesso');
              $window.scrollTo(0, 0);
            } else {
              toastr.error('Arquivo não encontrado!');
              return;
            }
          },
          function (response) {
            toastr.error('Falha na exportação dos dados');
          }
        );
      }
    };

    /**
     * Get the form versions of a form
     * @param  {} formSlug
     * @param  {} currentFormId
     */
    function populateFormRevisions (formSlug, currentFormId) {
      FormService.getFormRevisions(formSlug, currentFormId).then(
        function (response) {
          var revisions = response.data;
          for (var index = 0; index < revisions.length; index++) {
            var revision = revisions[index];
            revision.createdAtFormatted = $filter('date')(revision.createdAt, 'dd/MM/yyyy HH:mm:ss');
            revisions[index] = revision;
          }
          me.formRevisions = revisions;
        },
        function (response) {
          toastr.error('Falha ao recuperar histórico de versões do formulários');
        }
      );
    }

    /**
     * Switch to a form revision
     */
    me.switchToFormRevision = function () {
      me.showFormDetailsByFormId(me.formRevision.id);
    };

    /**
     * Exports all forms satisfying the filter combination
     */
    me.exportAll = function () {
      FormService.exportAll(me.formQuery).then(
        function (response) {
          if (response.data != 'false') {
            window.location.href = response.data;
            toastr.success('Dados exportados com sucesso');
            $window.scrollTo(0, 0);
          } else {
            toastr.error('Arquivo não encontrado!');
            return;
          }
        },
        function (response) {
          toastr.error('Falha na exportação dos dados');
        }
      );
    };

    /**
     * Selects all forms visible
     */
    me.toggleSelectAllVisible = function () {
      for (var index = 0; index < me.results.length; index++) {
        me.results[index].selected = !me.results[index].selected;
      }
    };

    /**
     * opens a latlong in Google Maps
     * @param  {} latlong
     */
    me.openMapUrl = function (latlong) {
      $window.open('http://maps.google.com/maps?q=' + latlong, '_blank');
    };
    $scope._formCreatedAtFrom = me.formCreatedAtFrom;
    
    me.change = function(dt) {
     console.log(dt);
    };

    /* datepicker*/
    $scope.today = function () {
      $scope.dt = new Date();
    };
    $scope.today();

    $scope.clear = function () {
      $scope.dt = null;
    };
    
    $scope.format = 'dd/MM/yyyy';
    $scope.formatMonth = 'MM/yyyy';
    $scope.altInputFormats = ['M!/d!/yyyy'];

    $scope.inlineOptions = {
      customClass: getDayClass,
      minDate: new Date(2016, 9, 1),
      showWeeks: true
    };

    $scope.dateOptions = {
    //Permite selecionar Sábados e Dominigos 
    // dateDisabled: disabled,
      formatYear: 'yy',
      maxDate: new Date(2100, 12, 31),
      minDate: new Date(2016, 9, 1),
      startingDay: 1
    };

    // Disable weekend selection
    function disabled (data) {
      var date = data.date,mode = data.mode;
      return mode === 'day' && (date.getDay() === 0 || date.getDay() === 6);
    }

    $scope.toggleMin = function () {
      $scope.inlineOptions.minDate = $scope.inlineOptions.minDate ? null : new Date(2016, 9, 1);
      $scope.dateOptions.minDate = $scope.inlineOptions.minDate;
    };

    $scope.toggleMin();

    /*controls the CreatedAtFrom */
    $scope.openCreatedAtFrom = function ($event) {
     
      $scope.popupCreatedAtFromOpen.opened = true;      
    };

    $scope.popupCreatedAtFromOpen = {
      opened: false
    };
    /*controls end the CreatedAtFrom */

    /*controls the createdAtTo */
    $scope.openCreatedAtTo = function () {
      $scope.popupCreatedAtToOpen.opened = true;
    };

     $scope.popupCreatedAtToOpen = {
      opened: false
    };
    /*end controls the createdAtTo */

    /*controls the FormSyncedAtFrom */
    $scope.openFormSyncedAtFrom = function () {
      $scope.popupFormSyncedAtFromOpen.opened = true;
    };

     $scope.popupFormSyncedAtFromOpen = {
      opened: false
    };
    /*end controls the FormSyncedAtFrom */

    /*controls the FormSyncedAt */
    $scope.openFormSyncedAtTo = function () {
      $scope.popupFormSyncedAtToOpen.opened = true;
    };

     $scope.popupFormSyncedAtToOpen = {
      opened: false
    };
    /*end controls the FormSyncedAt */

    /*controls the FilterValueDate */
    $scope.openFilterValueDate = function () {
      $scope.popupFilterValueDateOpen.opened = true;
    };

    $scope.popupFilterValueDateOpen = {
      opened: false
    };
    /*end controls the FilterValueDate */

     /*controls the FilterValueDate */
    $scope.openFilterValueMonth = function () {
      $scope.popupFilterValueMonthOpen.opened = true;
    };

    $scope.popupFilterValueMonthOpen = {
      opened: false
    };
    /*end controls the FilterValueDate */

    $scope.setDate = function (year, month, day) {
      $scope.dt = new Date(year, month, day);
    };


    

    

    var tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    var afterTomorrow = new Date();
    afterTomorrow.setDate(tomorrow.getDate() + 1);
    $scope.events = [
      {
        date: tomorrow,
        status: 'full'
      },
      {
        date: afterTomorrow,
        status: 'partially'
      }
    ];

    function getDayClass (data) {
      var date = data.date,
        mode = data.mode;
      if (mode === 'day') {
        var dayToCheck = new Date(date).setHours(0, 0, 0, 0);

        for (var i = 0; i < $scope.events.length; i++) {
          var currentDay = new Date($scope.events[i].date).setHours(0, 0, 0, 0);

          if (dayToCheck === currentDay) {
            return $scope.events[i].status;
          }
        }
      }

      return '';
    }
    /* end datepicker*/
  }
})();
