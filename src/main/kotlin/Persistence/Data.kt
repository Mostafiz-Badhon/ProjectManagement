package Persistence

class Data() {
    var projects: List<ProjectJSON> = listOf()

    constructor(projects: List<ProjectJSON>): this() {
        this.projects = projects
    }
}