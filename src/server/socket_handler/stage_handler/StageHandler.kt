package server.socket_handler.stage_handler

abstract class StageHandler(){
    abstract suspend fun start(stageData: StageData):StageData
}