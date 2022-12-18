package com.acosta.education.message.dashboard

import com.acosta.education.message.Message
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class DashboardService(@Autowired val messageClient: MessageClient) {
    fun getMessagesOfUser(userName: String): List<Message> {
        return messageClient.getMessagesOfUser(userName)
    }
}
