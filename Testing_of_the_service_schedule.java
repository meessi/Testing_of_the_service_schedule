import groovy.time.TimeCategory

def date = new Date(); // Текущая дата (+ время)
def serviceTime = utils.get('servicetime$101450623'); // Элемент справочника "Классы обслуживания"
def timeZone = utils.get('timezone$131801'); // Элемент справочника "Часовые пояса"

def case1 = ['Текущий день - рабочий, время - рабочее; следующий день - рабочий', '14.06.2017 16:00', 'Да'];
def case2 = ['Текущий день - рабочий, время - нерабочее; следующий день - рабочий', '14.06.2017 19:00', 'Да'];
def case3 = ['Текущий день - рабочий, время - рабочее;  следующий день - выходной', '16.06.2017 16:00', 'Да'];
def case4 = ['Текущий день - рабочий, время - нерабочее; следующий день - выходной', '16.06.2017 19:00', 'Да'];
def case5 = ['Текущий день - выходной, следующий день - выходной', '17.06.2017 16:00', 'Нет'];
def case6 = ['Текущий день - выходной, следующий день - рабочий', '18.06.2017 16:00', 'Нет'];

def cases = [case1, case2, case3, case4, case5, case6];
def results = '';

/*
* Следующая дата/время напоминания
*
*/
def nextDateTimeReminder =
{
	currentDateTime, nearestDateTimeService_ ->
	
  	def period = nearestDateTimeService_ - currentDateTime;
  
	use(TimeCategory)
	{
		return period < 2 ? (currentDateTime + 1.day - 1.minute) : (currentDateTime + period.day - 1.minute);
	}
}

/*
* Ближайшая дата/время обслуживания
*
*/
def nearestDateTimeService = 
{
	currentDateTime ->
		
	return api.timing.serviceStartTime(currentDateTime, serviceTime, timeZone); 
}

/*
* Текущий день является днем обслуживания?
*
*/
def currentDayIsService = 
{
	currentDateTime ->
	
	def currentDate = utils.formatters.strToDate(utils.formatters.formatDate(currentDateTime));
	def nearestDateTimeService_ = nearestDateTimeService(currentDate);
	def period = nearestDateTimeService_ - currentDate;
	
	return period == 0;
}

/*
* Перевод строки в дату
*
*/
def stringToDateTime =
{
	string ->
	
	return utils.formatters.strToDateTime(string);
}

for (def i = 0; i < 6; i++)
{
	def currentDateTime = stringToDateTime(cases[i][1]);
	def currentDayIsService_ = currentDayIsService(currentDateTime);
	def nearestDateTimeService_ = nearestDateTimeService(currentDateTime);
	def nextDateTimeReminder_ = nextDateTimeReminder(currentDateTime, nearestDateTimeService_);
	
	results = results + cases[i][0] + '. Текущее дата/время: ' + cases[i][1] + '. Отправка в текущий день? - ' + cases[i][2] + '. Будет ли она выполнена? - ' + (currentDayIsService_ == true ? 'Да' : 'Нет') + '. Ближайшая дата/время обслуживания: ' + utils.formatters.formatDateTime(nearestDateTimeService_) + '. Следующее напоминание: ' + utils.formatters.formatDateTime(nextDateTimeReminder_) + '<br><br><br>';
}

return results;