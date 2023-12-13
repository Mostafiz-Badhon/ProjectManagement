package Persistence

import Project
import Task
import com.google.gson.Gson
import java.io.File


fun Task.toJSON() = TaskJSON(name, previousTasks.map { t -> t.name }, nextTasks.map { t -> t.name }, duration, lag)
fun Project.toJSON() = ProjectJSON(name, "", tasks.map { t -> t.toJSON() })

object Persistence{
    val projects = mutableListOf<Project>()
    private var loading = true

    // Load data from JSON file if it exists
    init {
        val file = File("data.json")
        if(file.exists()) {
            try {
                val data = Gson().fromJson(file.readText(), Data::class.java)
                // Load data into lists. Using as*() methods adds them to Persistence.Persistence directly
                // (refer to Member, Team and Project constructors)
                data.projects.forEach { it.asProject() }
            } catch (e: Exception) {
                // If any error is encountered, clear all lists and throw exception.
                // E.g. couldn't parse JSON file
                projects.clear()
                loading = false
                throw Exception("Couldn't load data from disk, file could be corrupted.")
            }
        }
        loading = false
    }

    fun addProject(project: Project) {
        projects.add(project)
        save()
    }


    fun getProjectByName(projectName: String): Project? {
        return projects.find { it.name == projectName }
    }



    fun removeProject(project: Project) {
        projects.remove(project)
    }

    fun save() {
        if (!loading) {
            val toOutput = Data(projects.map { it.toJSON() })

            val jsonOutput: String = Gson().toJson(toOutput)

            File("data.json").writeText(jsonOutput)
        }
    }

    override fun toString() = """
        Projects: ${projects.map { "${it.name}, tasks: [${it.tasks.map { t -> t.name }}]" }}
    """.trimIndent()
}