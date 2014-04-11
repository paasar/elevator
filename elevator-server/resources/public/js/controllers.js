var renderApp = angular.module('renderApp', ['stateService']);

renderApp.controller('RenderController', ['$scope', '$timeout', 'GameState', function ($scope, $timeout, GameState) {
    $scope.states = GameState.query();

    var updateState = function () {
        $scope.states = GameState.query();
        $timeout(function() {updateState();}, 5000);

    };
    $timeout(function() {updateState();}, 5000);
}]);