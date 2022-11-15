package com.footlocer.mon.listener;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.List;

public class MessageEvent extends ListenerAdapter {
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        //如果是机器人发消息触发这个事件则忽略
        //如果不加，则这个事件会变成死循环事件
        if (event.getAuthor().isBot()) {
            return;
        }

        Message message = event.getMessage();

        //获取服务器（公会）信息
        Guild guild = message.getGuild();
        String guildId = guild.getId();
        String guildName = guild.getName();
        System.out.println("guildId = " + guildId);//输出服务器（公会）id
        System.out.println("guildName = " + guildName);//输出服务器（公会）名称

        //获取频道信息
        MessageChannel channel = message.getChannel();
        String channelId = channel.getId();
        String channelName = channel.getName();
        ChannelType channelType = channel.getType();
        System.out.println("channelId = " + channelId);//输出频道id
        System.out.println("channelName = " + channelName);//输出频道名称
        System.out.println("channelType = " + channelType);//输出频道类型

        //获取发消息人
        Member member = message.getMember();
        String memberId = member.getId();
        String memberNickname = member.getNickname();
        String memberEffectiveName = member.getEffectiveName();
        List<Role> memberRoles = member.getRoles();
        EnumSet<Permission> memberPermissions = member.getPermissions();
        OnlineStatus memberOnlineStatus = member.getOnlineStatus();
        System.out.println("memberId = " + memberId);//输出发送消息人的id
        System.out.println("memberNickname = " + memberNickname);//输出发送消息人的昵称
        System.out.println("memberEffectiveName = " + memberEffectiveName);//输出发送消息人的有效名称
        System.out.println("memberRoles = " + memberRoles);//输出发送消息人的角色列表
        System.out.println("memberPermissions = " + memberPermissions);//输出发送消息人的权限列表
        System.out.println("memberOnlineStatus = " + memberOnlineStatus);//输出发送消息人的在线状态

        //获取消息信息
        String messageId = message.getId();
        String messageContentRaw = message.getContentRaw();
        MessageType messageType = message.getType();
        System.out.println("messageId = " + messageId);//输出消息id
        System.out.println("messageContentRaw = " + messageContentRaw);//输出消息内容
        System.out.println("messageType = " + messageType);//输出消息类型

        //回应用户消息
        channel.sendMessage("bot already received").queue();
    }
}
