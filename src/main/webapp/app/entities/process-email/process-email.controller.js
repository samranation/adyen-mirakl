(function() {
    'use strict';

    angular
        .module('adyenMiraklConnectorApp')
        .controller('ProcessEmailController', ProcessEmailController);

    ProcessEmailController.$inject = ['DataUtils', 'ProcessEmail'];

    function ProcessEmailController(DataUtils, ProcessEmail) {

        var vm = this;

        vm.processEmails = [];
        vm.openFile = DataUtils.openFile;
        vm.byteSize = DataUtils.byteSize;

        loadAll();

        function loadAll() {
            ProcessEmail.query(function(result) {
                vm.processEmails = result;
                vm.searchQuery = null;
            });
        }
    }
})();
