/*
   Copyright 2009 NEERC team

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
// $Id$
/**
 * Date: 27.10.2004
 */
package ru.ifmo.neerc.chat;

import ru.ifmo.neerc.chat.message.Message;
import ru.ifmo.neerc.chat.message.TaskMessage;

import java.util.*;

/**
 * @author Matvey Kazakov
 */
public class TaskRegistry implements UserRegistryListener, MessageListener {
    
    private static TaskRegistry instance = new TaskRegistry();

    public static TaskRegistry getInstance() {
        return instance;
    }

    private TaskRegistry() {}

    private Map<Integer, Task> taskById = new HashMap<Integer, Task>();

    private Collection<TaskRegistryListener> listeners = new ArrayList<TaskRegistryListener>();

    public synchronized void registerTask(Task task) {
        taskById.put(task.getId(), task);
        for (Iterator<TaskRegistryListener> iterator = listeners.iterator(); iterator.hasNext();) {
            iterator.next().taskAdded(task);
        }
    }

    public synchronized void deleteTask(int taskId) {
        Task task = taskById.remove(taskId);
        if (task != null) {
            for (Iterator<TaskRegistryListener> iterator = listeners.iterator(); iterator.hasNext();) {
                iterator.next().taskDeleted(task);
            }
        }
    }

    public void userAdded(UserEntry userEntry) {
    }

    public void userChanged(UserEntry userEntry) {
    }

    public synchronized void userRemoved(UserEntry userEntry) {
    }

    public synchronized void assignTask(int taskId, int userId) {
        Task task = taskById.get(taskId);
        task.assign(userId);
        fireTaskChanged(task);
    }

    public synchronized void completeTask(int taskId, int userId, TaskResult result) {
        Task task = taskById.get(taskId);
        task.completeTask(userId, result);
        fireTaskChanged(task);
    }

    public synchronized Set<Task> getAssignedTasks(int userId) {
        Set<Task> result = new TreeSet<Task>();
        for (Task task : taskById.values()) {
            if (task.isAssigned(userId)) {
                result.add(task);
            }
        }
        return result;
    }

    public synchronized Set<Task> getTasks() {
        return new TreeSet<Task>(taskById.values());
    }

    public synchronized void addListener(TaskRegistryListener listenerTask) {
        listeners.add(listenerTask);
    }

    public synchronized void removeListener(TaskRegistryListener listenerTask) {
        listeners.remove(listenerTask);
    }

    private void fireTaskChanged(Task task) {
        for (Iterator<TaskRegistryListener> iterator = listeners.iterator(); iterator.hasNext();) {
            iterator.next().taskChanged(task);
        }
    }
    
    private static int TASK_ID = 0;

    public synchronized Task createTask(String taskDescription, int type) {
        int max = 0;
        for (Iterator<Integer> iterator = taskById.keySet().iterator(); iterator.hasNext();) {
            max = Math.max(iterator.next(), max);
        }
        return new Task(newTaskID(max), taskDescription, type);
    }

    private static synchronized int newTaskID(int max) {
        TASK_ID = Math.max(TASK_ID + 1, max + 1);
        return TASK_ID;
    }

    public synchronized void init(Task[] list) {
        if (list != null) {
            for (int i = 0; i < list.length; i++) {
                registerTask(list[i]);
            }
        }
    }

    public synchronized Task[] serialize() {
        return taskById.values().toArray(new Task[taskById.size()]);
    }


    public void processMessage(Message message) {
        if (message instanceof TaskMessage) {
            TaskMessage taskMessage = (TaskMessage)message;
            int taskId = taskMessage.getTaskId();
            int userId = taskMessage.getUser();
            switch (taskMessage.getTaskMsgType()) {
                case TaskMessage.ASSIGN:
                    assignTask(taskId, userId);
                    break;
                case TaskMessage.COMPLETE:
                    completeTask(taskId, userId, taskMessage.getAnswer());
                    break;
                case TaskMessage.CREATE:
                    registerTask(taskMessage.getTask());
                    break;
                case TaskMessage.DELETE:
                    deleteTask(taskId);
                    break;
            }
        }
    }

    public Task findTask(int taskId) {
        return taskById.get(taskId);
    }
}

