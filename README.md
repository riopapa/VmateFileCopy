# Vmate File Copy
**VmateFileCopy** is the android application to speak notification text using google TTS

- it supports table to convert application name into simple keyword
- it supports table to ignore some cases

<H1>Screenshots and How to operate</H1>

<H3>MainActivity</H3>

- When loaded, it reads various text files listed in next paragraph to ignore some notifications
- Notification icon and notification bar is settled
- Notification bar is for refresh(reload) above files and stop speaking temporary and immediately
- Speaking pitch and speed can be adjusted.
- Speaking will be executed when (1) no silent mode or (2) bluetooth or earphone is connected regardless of silent mode.

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
    <img src="./screenshots/mainActivity1.jpg" width=400 height=800>

<H3>Tables used</H3>

- Location : /sdcard/sayNotiText/tables
- Files in that directory

## Function : Convert vmate files into valid date time format

- Current Snoppa vmate creates file name with colon(:) and timezone is not local.
- Some video play app in android phone can NOT understand this colon, and spit out that video 
- This app will copy vmate media files into Camera Folder with more proper name.
- You need to set your timezone before starting copy (by clicking clock icon)

## How to operate

- When this app is loaded, it will display all the file list you downloaded from vmate app.
> On setting screen
>> set your time zone
>> choose whether source file will be deleted or not
>> file location information
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;from : \Vmate\sd/DCIM/100HSCAM
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;to : DCIM\vmate


## Notice

    - No guarantee for improving functions
    - This app copies vmate file into camera folder not renaming itself
    - This app will be disparated when snoppa vmate gives proper file name.

## Remarks

    Sorry but only available in **android** play store
    *Author* : Woncherl Ha, riopapa@gmail.com
    *Source* : [my github](https://github.com/riopapa/VmateFileCopy)
     
