/**
 * Camada de Abstração para comunicação com o servidor 
 **/
(function() {

	angular.module("app").service("FormService", ['$q', '$http','$filter', function($q, $http,$filter) {

        var service = {
            getAllTemplates: getAllTemplates,
            getFields: getFields,
            prepareFormDetails: prepareFormDetails,
            appendFilter: appendFilter,
            search:search,
            getOperators:getOperators,
            getDomainOptions:getDomainOptions,
            isDate:isDate,
            tryParseDate:tryParseDate,
            isNumber:isNumber,
            getCurrentUser:getCurrentUser,
            getCityUnities:getCityUnities,
            getUnityUsers:getUnityUsers,
            getFormSetByFormId: getFormSetByFormId,
            getFormSetByFormSlugAndDate: getFormSetByFormSlugAndDate,
            exportSelected:exportSelected,
            exportAll:exportAll,
            getFormRevisions:getFormRevisions
        };
		/**
		 * get all forms 
		 ***/
		function  getAllTemplates() {
			return $http.get('/forms/templates');
		}

        function getCurrentUser(){
            return $http.get('/usuarios/current');
        }
		
		/**
		 * get fields form 
		 **/
		function  getFields(idForm) {
			return $http.get('/forms/fields/'+idForm);
		}

        /**
		 * get city unities 
		 **/
		function  getCityUnities(idCity) {
			return $http.get('/unidadesaude/search/'+idCity+'/');
		}

          /**
		 * get city unities 
		 **/
		function  getUnityUsers(idUnity) {
			return $http.get('/usuarios/byunity/'+idUnity);
		}
		
		/**
		 * get data form 
		 **/
		function  getFormSetByFormId(idForm) {
            var deferred = $q.defer();
			$http.get('/forms/'+idForm).then(function(response){
    			var formSet = angular.copy(response.data);  
                adjustFormSet(formSet);
                deferred.resolve(formSet);  
    		},
            function error(reason){
                deferred.reject(reason);  
            });
            return deferred.promise;
		}

        /**
		 * get data form 
		 **/
		function  getFormSetByFormSlugAndDate(formSlug, formCreatedAt) {
            var deferred = $q.defer();
            var data = {'params': {'formSlug':formSlug, 'formCreatedAt':formCreatedAt}};
			$http.get('/forms/bysluganddate/',data).then(function(response){
    			var formSet = angular.copy(response.data);
                adjustFormSet(formSet);
                deferred.resolve(formSet);  
    		},
            function error(reason){
                deferred.reject(reason);  
            });
            return deferred.promise;
		}

        /**
         * Adjust a formSet (setting the formatted createdAt, syncedAt dates of the form) 
         * and also adjuste the exibition values of the formData collection 
         * @param  {} formSet
         */
        function adjustFormSet(formSet){            
            formSet.createdAt = tryParseDate(formSet.form.createdAt);
            formSet.createdAt = $filter('date')(formSet.createdAt,'dd/MM/yyyy HH:mm:ss');            
            formSet.synchronizedAt = tryParseDate(formSet.form.synchronizedAt);
            formSet.synchronizedAt = $filter('date')(formSet.synchronizedAt,'dd/MM/yyyy HH:mm:ss');
            var formDatas = formSet.formData;                                
            prepareFormDetails(formDatas);
            formSet.formData = formDatas;             
        }


        /**
         * Adjust the exibition value of each formData item according its datatype
         * @param  {} formDataCollection
         */
        function prepareFormDetails(formDataCollection){
            for (var index = 0; index < formDataCollection.length; index++) {
                    var formData = formDataCollection[index];
                    if(formData.dataType == 'date' || formData.dataType == 'month' ){
                        formatFormDate(formData);
                        formData.exhibitionType = 'data';
                        formDataCollection[index] =  formData;                                                
                    } 
                    else if(formData.dataType == "associatedFormType"){                        
                        adjustAssociatedFormType(formData);
                        formData.exhibitionType = 'formChildList';
                        formDataCollection[index]  = formData;                    
                    } 
                    else if(formData.dataType == "slug"){                        
                        formData.exhibitionType = 'formReference';
                        formDataCollection[index]  = formData;                     
                    } 
                    else if(formData.dataType == "latlong"){                        
                        formData.exhibitionType = 'locationMap';
                        formDataCollection[index]  = formData;                     
                    } 
                    else{
                        if(formData.valueDesc === null || formData.valueDesc === undefined || formData.valueDesc === ''){
                            formData.valueDesc = 'Não informado(a)';
                        }
                        formData.exhibitionType = 'data';
                        formDataCollection[index]  = formData;
                    }                   
                }
        }


        /**
         * Formats the exibition of fields with date/month
         * @param  {} formData
         */
        function formatFormDate(formData){
            var dataValue = tryParseDate(formData.value);
            if(formData.dataType == 'month'){
                dataValue = $filter('date')(dataValue,'MM/yyyy'); 
            }
            else{
                dataValue = $filter('date')(dataValue,'dd/MM/yyyy'); 
            }
            
            formData.valueDesc =  dataValue;
        }

        /**
         * Adjusts the properties of a formData that is a list of child forms
         * @param  {} formData
         */
        function adjustAssociatedFormType(formData){
            if(!formData.desc && formData.valueDesc){
              formData.desc =angular.copy(formData.valueDesc);
            }
            
            if(formData.childForms && formData.childForms.length > 0){
                formData.valueDesc = formData.childForms.length;
                formData.hasChild = true;
            }
            else{
                formData.childForms = [];
                formData.hasChild = false;
                
                formData.valueDesc = 'nenhum(a)';
            }  
            formData.exhibitionType = 'formChildList';
            
        }

        
        /**
         * Appends a filter for a formQuery, making the necessary adjustments in the values
         * Each call to appendfilter will generate internally two filter, considering that it snecessary filter a 
         * form based in the field slug and field value
         * @param  {} filter
         * @param  {} filters
         * @param  {} viewFilters
         * @param  {} fieldDataType
         */
        function  appendFilter(filter, filters, viewFilters,fieldDataType){
            //filter colelction to use in the back end 
            var viewValue = filter.value;
            var value = filter.value;
            if( typeof(filter.value) === 'object' && !(filter.value instanceof Date)){
                viewValue = filter.value.desc;
                value = filter.value.value;
            }  
            if( value instanceof Date){
                value = $filter('date')(value,'yyyy-MM-dd');
                viewValue = $filter('date')(value,'dd/MM/yyyy');
            }    	
            var filterSlug = {
                column:'slug',                
                operator:"EQUAL",              
                value:filter.field.slug
            };
    		filters.push(filterSlug);

            var filterValue = {                                         
                operator:filter.operator.key,                
                value:value,
                dataType:fieldDataType
            };
            filters.push(filterValue);

            //filter collection to show in view
            var viewfilter = {               
                description:filter.field.description,               
                operatorDesc:filter.operator.value,
                value:viewValue
            };
            viewFilters.push(viewfilter);
        }

        /**
		 * Search forms by specified formquery
		 **/
		function  search(formQuery) {
			console.log(formQuery);
			
            var deferred = $q.defer();
			$http.get('/forms/search',{params:{formQuery: formQuery}}).then(function(response){    			
                deferred.resolve(response);  
    		},
            function error(reason){
                deferred.reject(reason);  
            });
            return deferred.promise;
		}

		/**
		 * Gets the operators list
		 **/
		function getOperators(dataType) {
			return $http.get('/forms/operators/' + dataType);
		}

        /**
		 * Gets the operators list
		 **/
		function getFormRevisions(formSlug, currentFormId) {
			return $http.get('/forms/revisions/' + formSlug + '/' + currentFormId); 
		}

        /**
		 * List domain data from specified domain
		 **/
		function  getDomainOptions (domainSlug) {
			return $http.get('/forms/domainoptions/' + domainSlug);
		}

        /**
		* List domain data from specified domain
		**/
		function  exportSelected (FormExport) {
			return $http.post('/forms/exportselected', JSON.stringify(FormExport));
		}

        /**
		* List domain data from specified domain
		**/
		function  exportAll (formQuery) {
			return $http.post('/forms/exportall', JSON.stringify(formQuery));
		}
		
        /**
         * Checks whenever a var is a representation of a date
         * @param  {} content
         * @returns boolean
         */
        function isDate (content){
            var isInt = isNumber(content);
            var tryedDate = null;
            if(!isInt){
                tryedDate = Date.parse(content);
                if(!isNaN(tryedDate)){
                    return true;
                }
            }
            else{
                tryedDate = new Date(content);
                if(!isNaN(tryedDate)){
                    return true;
                }
            }
            return false;
        }

        /**
         * Tries to parse a date from the givem content
         * @returns boolean|Date object
         * @param  {} content
         */
        function  tryParseDate(content){
            var isInt = isNumber(content);
            var tryedDate = null;
            if(!isInt){
                tryedDate = Date.parse(content);
                if(!isNaN(tryedDate)){
                    return new Date(tryedDate);
                }
            }
            else{
                tryedDate = new Date(content);
                if(!isNaN(tryedDate)){
                    return tryedDate;
                }
            }
            return false;
        }

        /**
         * Checks whenever the content is a number
         * @returns boolean
         * @param  {} content
         */
        function isNumber (content){
            return /^-?[\d.]+(?:e-?\d+)?$/.test(content);
        }	
        
        
        
        return service;	

		
	} ]);
})();