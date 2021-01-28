package bot.botApi;

import bot.models.openweathermodel.FullModelWeather;
import bot.models.openweathermodel.SimpleModelWeather;
import bot.parser.WeatherRequest;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component("TelegramBot")
public class Bot extends TelegramLongPollingBot {

    private Environment environment;

    public Bot() {
    }

    public Bot(@Qualifier("MyEnvironment")Environment environment) {
        this.environment = environment;
    }

    @Override
    public String getBotToken() {
        return environment.getProperty("bot.token");
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        if (message != null && message.hasText()){
            switch (message.getText()){
                case "/help":
                    //execute(sendMessage.setText(message.getText() + "how can i help"));
                    sendMsg(message,"Введите город (иностранные на английском)");
                    break;
                case "/settings":
                    sendMsg(message,"Что будем настраивать");
                    break;
                default:
                    try {
                        sendMsg(message, WeatherRequest.getWeather(message.getText(), new SimpleModelWeather()));
                    } catch (IOException e) {
                        sendMsg(message,"Город не найден");
                    }
                    //sendMsg(message,"Неизвестная команда");
            }
        }
    }

    @Override
    public String getBotUsername() {
        return environment.getProperty("bot.username");
    }

    //Перенести в сервис
    private void sendMsg(Message message, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setText(text);
        try {
            setButtons(sendMessage);
            execute(sendMessage.setText(text));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        /*
        * depricated
        try {
            sendMessage(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        * */
    }

    public void setButtons(SendMessage sendMessage){
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow keyRow1 = new KeyboardRow();

        keyRow1.add(new KeyboardButton("/help"));
        keyRow1.add(new KeyboardButton("/settings"));

        keyboardRows.add(keyRow1);
        replyKeyboardMarkup.setKeyboard(keyboardRows);
    }
}
