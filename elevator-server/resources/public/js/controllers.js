var renderGameApp = angular.module('renderGameApp', ['stateService', 'gameFilters']);

renderGameApp.controller('GameRenderController', ['$scope', '$timeout', 'GameState', '$q', '$filter',
    function ($scope, $timeout, GameState, $q, $filter) {
        $scope.states = GameState.query();

        $scope.sort = false;
        $scope.toggleSort = function() {
            $scope.sort = !$scope.sort;
        };

        var overallScorePredicate = function(state) {
            return state.tally.overall;
        };

        var updateState = function () {
             var newStates = GameState.query();
            // Wait for the response to prevent blinking.
            $q.all([
                newStates.$promise
            ]).then(function() {
                if ($scope.sort) {
                    var orderBy = $filter('orderBy');
                    $scope.states = orderBy(newStates, overallScorePredicate, true);
                } else {
                    $scope.states = newStates;
                }
            });
            $timeout(function() {updateState();}, 1000);

        };
        $timeout(function() {updateState();}, 1000);
}]);

var renderAdminApp = angular.module('renderAdminApp', ['stateService']);

renderAdminApp.controller('AdminRenderController', ['$scope', '$timeout', 'GameInternalState', '$q', '$http',
        function ($scope, $timeout, GameInternalState, $q, $http) {
    $scope.state = GameInternalState.query();

    $scope.stop = function() {
        console.log("Calling pause.");
        $http.get("/stop");
    };

    $scope.start = function() {
        console.log("Calling continue.");
        $http.get("/start")
    };

    $scope.reset = function() {
        console.log("Calling reset.");
        $http.get("/reset")
    };

    $scope.deletePlayer = function(ip, port) {
        console.log("deleting player: " + ip + " " + port +"!");
        $http.delete("/player/" + ip + "/" + port);
    };

    var updateState = function () {
        var newState = GameInternalState.query();
        // Wait for the response to prevent blinking.
        $q.all([
            newState.$promise
        ]).then(function() {
            $scope.state = newState;
        });
        $timeout(function() {updateState();}, 1000);

    };
    $timeout(function() {updateState();}, 1000);
}]);