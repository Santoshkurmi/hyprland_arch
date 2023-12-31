#!/bin/bash

mount -o rw,remount /
location=/config/usb_gadget/g1/functions/

mass_storage_name=$(ls $location | grep mass_storage)

location=/config/usb_gadget/g1/functions/$mass_storage_name/lun.0

usb_controller=$(getprop sys.usb.controller)

gadget_path=/config/usb_gadget/g1

gadget_path_config=/config/usb_gadget/g1/configs/b.1

usb_mode_file=UDC

# echo "" > $gadget_path/$usb_mode_file

changeUsbMode(){
    # if [[ $1 == "off" ]];then
        echo "" > $gadget_path/$usb_mode_file
        sleep 3
    # else
        # echo "on"
        echo $usb_controller > $gadget_path/$usb_mode_file
        isEnabled=$(cat $gadget_path/$usb_mode_file)
        if [[ $isEnabled == $usb_controller ]];then
            echo "Mass storage is enabled"
        else 
            echo "Mass storage is disabled"
        fi
    # fi
}

changeMassStorageMode(){
    if [[ $1 == "on" ]];then
        ln -s $location $gadget_path_config/$mass_storage_name 2> /dev/null
    else 
        rm $gadget_path_config/$mass_storage_name 2> /dev/null
    fi
}

setReadWrite(){
    echo $1 > $location/ro 2> /dev/null
}


setCdrom(){
    echo $1 > $location/cdrom
}

setFile(){
    echo $1 > $location/file
}

print(){
    msg=$(cat $location/$1)
    echo "ISO=> "+$msg
}

read(){
    if [ -z "$1" ] 
    then
        echo "Please input the iso file.."
    else
        setReadWrite 1
        setCdrom 0
        setFile $1
        changeMassStorageMode on
        setprop sys.usb.config mass_storage
        changeUsbMode
        print file
    fi
}


 write(){
    if [ -z "$1" ];then
        echo "Please input the iso file.."
    else
        setReadWrite 0
        setCdrom 0
        setFile $1
        changeMassStorageMode on
        setprop sys.usb.config mass_storage
        changeUsbMode
        print file
    fi
 }

 floppy(){
    if [ -z "$1" ];then
        echo "Please input the iso file.."
    else
        setReadWrite 1
        setCdrom 1
        setFile $1
        changeMassStorageMode on
        setprop sys.usb.config mass_storage
        changeUsbMode
        print file
    fi
 }

  off(){
        setReadWrite 0
        setCdrom 0
        # setFile ""
        changeMassStorageMode off
        setprop sys.usb.config mtp
        changeUsbMode
  }

 execute(){
    file=$2
    case $1 in

    read|0)
    read $file
    ;;
    write|1)
    write $file 
    ;;
    floppy|2)
    floppy $file 
    ;;
    off)
    off 
    ;;
    --help|-h) 
    echo "boot read(0)|write(1)|floppy(2)   file.iso"
    echo "boot  file.iso #for write default"
    echo "example:  boot write /sdcard/ubuntu.iso"
    echo "example:  boot  0 /sdcard/ubuntu.iso"
    ;;
    *)
    read $1
    ;;
    esac
}
execute $1 $2