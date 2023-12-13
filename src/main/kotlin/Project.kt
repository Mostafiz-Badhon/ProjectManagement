import Persistence.Persistence

class Project(projectName: String) {
    //  name can not be empty.


    var name = ""
        set(value) {
            field = value.trim().takeIf { it.isNotEmpty() } ?: throw Exception("Project name cannot be blank.")
            Persistence.save()
        }

    val tasks = mutableSetOf<Task>()

    init {
        name = projectName
        Persistence.addProject(this)
    }

    /**
     * Introduces a new task to the project.
     * Previous tasks are an optional parameter (omit if a task has no dependencies).
     * @param taskName Name of the task
     * @param taskDuration Duration of the task
     * @param taskLag Optional delay before task starts
     * @throws Exception if the task name already exists within this project, or if specified dependencies are not found.
     */
    fun addTask(name: String, duration: Int, lag: Int = 0, vararg previousTasks: String) {
        if(tasks.find { t -> t.name == name } !== null)
            throw Exception("Each task name within a project must be distinct.")

        val dependencies = tasks.filter { t -> previousTasks.contains(t.name) }

        if(dependencies.size != previousTasks.size)
            throw Exception("Some or all dependent tasks do not exist")

        val newTask = Task(name, duration, dependencies.toMutableSet(), lag)

        tasks.add(newTask)
        Persistence.save()
    }


    fun editTask(name: String, newName: String? = null, newDuration: Int? = null, newLag: Int? = null) {
        val trimmedName = name.trim()

        if(trimmedName.isEmpty())
            throw Exception("Task name cannot be empty")

        val task = tasks.find { t -> t.name === trimmedName }
                ?: throw Exception("Task does not exist")

        // Change properties if they have been set
        if(newName !== null) {
            if(tasks.find { it.name == newName.trim() } !== null)
                throw Exception("Another task is using this name already.")
            task.name = newName
        }
        if(newDuration !== null) task.duration = newDuration
        if(newLag !== null) task.lag = newLag

        Persistence.save()
    }

    /**
     * Deletes a task by its name.
     * @param name The name of the task to be deleted
     * @throws Exception if task doesn't exist or task has successor tasks.
     */
    fun deleteTask(name: String) {
        val trimmedName = name.trim()

        if(trimmedName.isEmpty())
            throw Exception("Task name cannot be empty")

        val task = tasks.find { t -> t.name === trimmedName }
                ?: throw Exception("Task does not exist")

        if(task.nextTasks.isNotEmpty())
            throw Exception("Other tasks depend on this task. Delete them first")

        task.previousTasks.forEach { t -> t.nextTasks.remove(task) }

        tasks.remove(task)
        Persistence.save()
    }

    fun deleteProject() {
        val projectInPersistence = Persistence.getProjectByName(name)
            ?: throw Exception("Project not found in Persistence")

        if (projectInPersistence !== this) {
            throw Exception("Inconsistent state: The project in Persistence does not match the current project instance.")
        }

        // Remove the project from Persistence
        Persistence.removeProject(this)

        // Remove dependencies in other tasks
        tasks.forEach { task ->
            task.previousTasks.forEach { dependentTask ->
                dependentTask.nextTasks.remove(task)
            }
        }

        // Clear the task list
        tasks.clear()

        // Save changes to Persistence
        Persistence.save()
    }

    fun editProject(projectName: String) {
        val projectInPersistence = Persistence.getProjectByName(name)
            ?: throw Exception("Project not found in Persistence")

        if (projectInPersistence !== this) {
            throw Exception("Inconsistent state: The project in Persistence does not match the current project instance.")
        }

        val newName = projectName.trim();

        if(newName.isNotEmpty()){
            name = newName;
        }

        // Save changes to Persistence
        Persistence.save()
    }

    fun createAdjacencyMatrix(): Array<BooleanArray> {
        // Create a mapping of task names to their indices
        val taskIndexMap = tasks.mapIndexed { index, task -> task.name to index }.toMap()

        // Initialize the adjacency matrix with false values
        val adjacencyMatrix = Array(tasks.size) { BooleanArray(tasks.size) { false } }

        // Populate the matrix based on task dependencies
        tasks.forEachIndexed { rowIndex, task ->
            task.nextTasks.forEach { dependentTask ->
                val colIndex = taskIndexMap[dependentTask.name] ?: throw Exception("Task index not found")

                // Set the corresponding entry in the matrix to true
                adjacencyMatrix[rowIndex][colIndex] = true
            }
        }

        return adjacencyMatrix
    }

    fun getTaskIndex(): Map<String, Int> {
        return tasks.mapIndexed { index, task -> task.name to index }.toMap()
    }





}