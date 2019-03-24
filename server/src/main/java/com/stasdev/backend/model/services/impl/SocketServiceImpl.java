package com.stasdev.backend.model.services.impl;

import com.stasdev.backend.model.entitys.*;
import com.stasdev.backend.model.repos.ApplicationUserRepository;
import com.stasdev.backend.model.repos.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


/*
* TODO заекстрактить интерфейс
* Так как сокеты никак не защищены перед отправкой чистим всю чувствительную инфу
*
* */
@Service
@Transactional
public class SocketServiceImpl {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ApplicationUserRepository applicationUserRepository;
    private final RoleRepository roleRepository;

    @Autowired
    public SocketServiceImpl(SimpMessagingTemplate simpMessagingTemplate, ApplicationUserRepository applicationUserRepository, RoleRepository roleRepository) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.applicationUserRepository = applicationUserRepository;
        this.roleRepository = roleRepository;
    }

    public void sendPushAboutTransaction(Transaction transaction, String userName){
        if (!userName.equals(transaction.getUserFrom())){
            simpMessagingTemplate.convertAndSend("/topic/push/"+userName, new Push("Your transaction was successful"));
        }
        simpMessagingTemplate.convertAndSend("/topic/push/"+transaction.getUserFrom(), new Push("Your transaction was successful"));
        simpMessagingTemplate.convertAndSend("/topic/push/"+transaction.getUserTo(),  new Push("User '"+transaction.getUserFrom()+"' sent money for you"));
    }

    /*
    * Данный метод делает рассылку всем админам и непосредственно участникам транзакции
    * */
    public void sendPushWithUpdatedAccounts(final Account accountFrom,final Account accountTo) {
        Role admin = roleRepository.findByRole("admin").orElseThrow(() -> new RuntimeException("Not have role admin"));
        Account accountClearFrom = clearSensitiveInformation(accountFrom);
        Account accountClearTo = clearSensitiveInformation(accountTo);
        applicationUserRepository.findAll()
                .stream()
                .filter(u -> u.getRoles().contains(admin))
                .forEach(a -> {simpMessagingTemplate.convertAndSend("/topic/accounts/"+a.getUsername(), accountClearFrom); simpMessagingTemplate.convertAndSend("/topic/accounts/"+a.getUsername(), accountClearTo);});
        if (!accountFrom.getUser().getRoles().contains(admin)){
            simpMessagingTemplate.convertAndSend("/topic/accounts/"+accountFrom.getUser().getUsername(), accountClearFrom);
        }
        simpMessagingTemplate.convertAndSend("/topic/accounts/"+accountTo.getUser().getUsername(), accountClearTo);
    }

    private Account clearSensitiveInformation(Account account){
        Account account1 = new Account();
        account1.setId(account.getId());
        account1.setAmount(account.getAmount());
        account1.setName(account.getName());
        return account1;
    }


}
